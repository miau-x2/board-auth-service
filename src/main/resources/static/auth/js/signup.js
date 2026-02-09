(() => {
    // =========================
    // DOM
    // =========================
    const $ = (id) => document.getElementById(id);

    const form = $("signupForm");
    const signupBtn = $("signupBtn");

    const usernameInput = $("username");
    const passwordInput = $("password");
    const emailInput = $("email");
    const nicknameInput = $("nickname");

    const otpInput = $("otp");
    const sendOtpBtn = $("sendOtpBtn");
    const verifyOtpBtn = $("verifyOtpBtn");

    const usernameFeedback = $("usernameFeedback");
    const passwordFeedback = $("passwordFeedback");
    const emailFeedback = $("emailFeedback");
    const nicknameFeedback = $("nicknameFeedback");
    const otpFeedback = $("otpFeedback");

    const globalErrorJs = $("globalErrorJs");

    const otpTimers = $("otpTimers");
    const otpTimerSpan = $("otpTimer");
    const resendTimerSpan = $("resendTimer");

    const togglePasswordBtn = $("togglePasswordBtn");
    const togglePasswordIcon = $("togglePasswordIcon");

    // =========================
    // 입력값 검증 규칙
    // =========================
    const USERNAME_MIN = 5;
    const USERNAME_MAX = 20;
    const USERNAME_REGEX = /^(?=.*[a-z])[a-z0-9]+$/;

    const PASSWORD_MIN = 8;
    const PASSWORD_MAX = 20;
    const PASSWORD_REGEX =
        /^(?=\S+$)(?=.*[A-Za-z])(?=.*\d)(?=.*[!@#$%^&*()])[A-Za-z\d!@#$%^&*()]+$/;

    const NICKNAME_MIN = 2;
    const NICKNAME_MAX = 20;
    const NICKNAME_REGEX = /^[a-z0-9가-힣]+$/;

    const OTP_REGEX = /^\d{6}$/;

    // =========================
    // 상태
    // =========================
    const state = {
        usernameOk: false,
        passwordOk: false,
        emailOk: false,
        nicknameOk: false,

        emailVerified: false,
        verifiedEmail: null, // OTP 검증 완료된 이메일(정규화된 값)

        lastUsernameChecked: null,
        lastEmailChecked: null,
        lastNicknameChecked: null,

        usernameReqSeq: 0,
        emailReqSeq: 0,
        nicknameReqSeq: 0,

        otpIntervalId: null,
        resendIntervalId: null,
        otpRemainingSec: 0,
        resendRemainingSec: 0,
    };

    let submitting = false;

    // =========================
    // 공통 유틸
    // =========================
    function clearGlobalError() {
        if (!globalErrorJs) return;
        globalErrorJs.hidden = true;
        globalErrorJs.textContent = "";
    }

    function showGlobalError(message) {
        if (!globalErrorJs) return;
        globalErrorJs.textContent = message || "요청 처리 중 오류가 발생했습니다.";
        globalErrorJs.hidden = false;
    }

    function clearFieldFeedback(feedbackEl, inputEl) {
        if (feedbackEl) {
            feedbackEl.textContent = "";
            feedbackEl.classList.remove("is-valid", "is-invalid");
        }
        if (inputEl) {
            inputEl.classList.remove("is-valid", "is-invalid");
        }
    }

    function setFeedback(feedbackEl, inputEl, message, ok) {
        if (!feedbackEl) return;

        feedbackEl.textContent = message || "";
        feedbackEl.classList.toggle("is-valid", !!ok);
        feedbackEl.classList.toggle("is-invalid", !ok);

        if (inputEl) {
            inputEl.classList.toggle("is-valid", !!ok);
            inputEl.classList.toggle("is-invalid", !ok);
        }
    }

    function formatMMSS(sec) {
        const s = Math.max(0, Number(sec) || 0);
        const mm = String(Math.floor(s / 60)).padStart(2, "0");
        const ss = String(s % 60).padStart(2, "0");
        return `${mm}:${ss}`;
    }

    function getCsrfHeader() {
        const token = form?.querySelector('input[name="_csrf"]')?.value;
        return token ? { "X-CSRF-TOKEN": token } : {};
    }

    async function safeFetchJson(url, options) {
        try {
            const res = await fetch(url, { credentials: "same-origin", ...options });
            const text = await res.text();
            const json = text ? JSON.parse(text) : null;
            return { res, json };
        } catch {
            showGlobalError("네트워크 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
            return { res: null, json: null };
        }
    }

    // ApiResponse.success는 “API 처리 성공”일 뿐.
    // 실제 사용 가능 여부는 data.available
    function parseAvailability(json) {
        const available = !!json?.data?.available;
        const message = json?.data?.message ?? json?.message ?? "";
        return { available, message };
    }

    function updateSendOtpButtonState() {
        // emailOk + 미인증 + 쿨다운 없음일 때만 enable
        const canSend = state.emailOk && !state.emailVerified && state.resendRemainingSec <= 0;
        if (sendOtpBtn) sendOtpBtn.disabled = !canSend;
    }

    // =========================
    // OTP UI / 타이머
    // =========================
    function stopOtpTimers() {
        if (state.otpIntervalId) clearInterval(state.otpIntervalId);
        if (state.resendIntervalId) clearInterval(state.resendIntervalId);
        state.otpIntervalId = null;
        state.resendIntervalId = null;
        state.otpRemainingSec = 0;
    }

    function resetOtpUi({ clearOtpValue = true } = {}) {
        stopOtpTimers();

        // 새 이메일로 다시 인증해야 하므로 쿨다운도 리셋
        state.resendRemainingSec = 0;

        if (otpInput) {
            if (clearOtpValue) otpInput.value = "";
            otpInput.disabled = true;
            otpInput.readOnly = false;
        }
        if (verifyOtpBtn) verifyOtpBtn.disabled = true;

        if (otpTimers) otpTimers.hidden = true;
        if (otpTimerSpan) otpTimerSpan.textContent = "--:--";
        if (resendTimerSpan) resendTimerSpan.textContent = "--:--";

        clearFieldFeedback(otpFeedback, otpInput);
    }

    function startOtpCountdown(otpValiditySeconds, cooldownSeconds) {
        stopOtpTimers();

        state.otpRemainingSec = Number(otpValiditySeconds) || 0;
        state.resendRemainingSec = Number(cooldownSeconds) || 0;

        if (otpTimers) otpTimers.hidden = false;
        if (otpTimerSpan) otpTimerSpan.textContent = formatMMSS(state.otpRemainingSec);
        if (resendTimerSpan) resendTimerSpan.textContent = formatMMSS(state.resendRemainingSec);

        // OTP 만료 타이머
        state.otpIntervalId = setInterval(() => {
            state.otpRemainingSec -= 1;
            if (otpTimerSpan) otpTimerSpan.textContent = formatMMSS(state.otpRemainingSec);

            if (state.otpRemainingSec <= 0) {
                clearInterval(state.otpIntervalId);
                state.otpIntervalId = null;
                if (otpTimerSpan) otpTimerSpan.textContent = "00:00";
                setFeedback(otpFeedback, otpInput, "인증번호가 만료되었습니다. 다시 요청해주세요.", false);
            }
        }, 1000);

        // 재전송 쿨다운
        if (sendOtpBtn) sendOtpBtn.disabled = true;

        state.resendIntervalId = setInterval(() => {
            state.resendRemainingSec -= 1;
            if (resendTimerSpan) resendTimerSpan.textContent = formatMMSS(state.resendRemainingSec);

            if (state.resendRemainingSec <= 0) {
                clearInterval(state.resendIntervalId);
                state.resendIntervalId = null;
                if (resendTimerSpan) resendTimerSpan.textContent = "00:00";
                updateSendOtpButtonState();
            }
        }, 1000);
    }

    // =========================
    // 이메일 변경 처리: input에서만 "즉시 인증 초기화"
    // =========================
    function normalizeEmailInput() {
        const raw = (emailInput?.value || "").trim();
        const v = raw.toLowerCase();
        if (emailInput) emailInput.value = v;
        return v;
    }

    function invalidateEmailCheckIfTypingChanged(currentEmail) {
        // 사용자가 타이핑 중이면 기존 도메인 검증 결과를 무효화
        if (state.lastEmailChecked && state.lastEmailChecked !== currentEmail) {
            state.emailOk = false;
            state.lastEmailChecked = null;
            clearFieldFeedback(emailFeedback, emailInput);
        }
    }

    function resetVerificationIfEmailChanged(currentEmail) {
        // 인증 완료 상태에서 이메일이 달라지면 인증 초기화
        if (!state.emailVerified) return;

        // 인증 초기화
        state.emailVerified = false;
        state.verifiedEmail = null;

        // 새 이메일로 다시 인증해야 하므로: OTP/타이머/쿨다운 전부 리셋
        resetOtpUi({ clearOtpValue: true });

        // 이메일 도메인 검증 결과도 무효화(안전)
        state.emailOk = false;
        state.lastEmailChecked = null;
        clearFieldFeedback(emailFeedback, emailInput);

        setFeedback(otpFeedback, otpInput, "이메일이 변경되어 인증이 초기화되었습니다. 다시 인증해주세요.", false);

        updateSendOtpButtonState();
    }

    // =========================
    // 1) 아이디 blur 검증
    // =========================
    async function validateUsername({ server = true } = {}) {
        clearGlobalError();
        const v = (usernameInput?.value || "").trim();

        if (!v) {
            state.usernameOk = false;
            setFeedback(usernameFeedback, usernameInput, "아이디를 입력해주세요.", false);
            return false;
        }
        if (v.length < USERNAME_MIN || v.length > USERNAME_MAX) {
            state.usernameOk = false;
            setFeedback(usernameFeedback, usernameInput, "아이디는 5~20자입니다.", false);
            return false;
        }
        if (!USERNAME_REGEX.test(v)) {
            state.usernameOk = false;
            setFeedback(usernameFeedback, usernameInput, "아이디는 영문 소문자와 숫자만 가능하며 영문은 필수입니다.", false);
            return false;
        }

        if (!server) {
            state.usernameOk = false;
            setFeedback(usernameFeedback, usernameInput, "아이디 중복 확인이 필요합니다.", false);
            return false;
        }

        if (state.lastUsernameChecked === v && state.usernameOk) return true;

        const seq = ++state.usernameReqSeq;
        const { res, json } = await safeFetchJson(
            `/auth/members/check-username?username=${encodeURIComponent(v)}`,
            { method: "GET", headers: { Accept: "application/json", ...getCsrfHeader() } }
        );

        if (seq !== state.usernameReqSeq) return false;

        if (!res || !res.ok || !json) {
            state.usernameOk = false;
            setFeedback(usernameFeedback, usernameInput, json?.message ?? "요청 처리 중 오류가 발생했습니다.", false);
            return false;
        }

        const { available, message } = parseAvailability(json);
        state.usernameOk = available;
        state.lastUsernameChecked = v;
        setFeedback(usernameFeedback, usernameInput, message, available);
        return available;
    }

    // =========================
    // 2) 비밀번호 blur 검증
    // =========================
    function validatePassword() {
        clearGlobalError();
        const v = passwordInput?.value ?? "";

        if (!v.trim()) {
            state.passwordOk = false;
            setFeedback(passwordFeedback, passwordInput, "비밀번호를 입력해주세요.", false);
            return false;
        }
        if (v.length < PASSWORD_MIN || v.length > PASSWORD_MAX) {
            state.passwordOk = false;
            setFeedback(passwordFeedback, passwordInput, "비밀번호는 8~20자입니다.", false);
            return false;
        }
        if (!PASSWORD_REGEX.test(v)) {
            state.passwordOk = false;
            setFeedback(
                passwordFeedback,
                passwordInput,
                "비밀번호는 영문, 숫자, 특수문자('!', '@', '#', '$', '%', '^', '&', '*', '(', ')')를 각각 1개 이상 포함해야 하며 공백은 사용할 수 없습니다.",
                false
            );
            return false;
        }

        state.passwordOk = true;
        setFeedback(passwordFeedback, passwordInput, "사용 가능한 비밀번호입니다.", true);
        return true;
    }

    // =========================
    // 3) 이메일 blur 검증 (blur에서는 검증만!)
    // =========================
    async function validateEmail({ server = true } = {}) {
        clearGlobalError();
        const v = normalizeEmailInput();

        if (!v) {
            state.emailOk = false;
            setFeedback(emailFeedback, emailInput, "이메일을 입력해주세요.", false);
            updateSendOtpButtonState();
            return false;
        }

        const emailLike = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v);
        if (!emailLike) {
            state.emailOk = false;
            setFeedback(emailFeedback, emailInput, "이메일 형식이 올바르지 않습니다.", false);
            updateSendOtpButtonState();
            return false;
        }

        if (!server) {
            state.emailOk = false;
            setFeedback(emailFeedback, emailInput, "이메일 중복 확인이 필요합니다.", false);
            updateSendOtpButtonState();
            return false;
        }

        if (state.lastEmailChecked === v && state.emailOk) {
            updateSendOtpButtonState();
            return true;
        }

        const seq = ++state.emailReqSeq;
        const { res, json } = await safeFetchJson(
            `/auth/members/check-email?email=${encodeURIComponent(v)}`,
            { method: "GET", headers: { Accept: "application/json", ...getCsrfHeader() } }
        );

        if (seq !== state.emailReqSeq) return false;

        if (!res || !res.ok || !json) {
            state.emailOk = false;
            setFeedback(emailFeedback, emailInput, json?.message ?? "요청 처리 중 오류가 발생했습니다.", false);
            updateSendOtpButtonState();
            return false;
        }

        const { available, message } = parseAvailability(json);
        state.emailOk = available;
        state.lastEmailChecked = v;

        setFeedback(emailFeedback, emailInput, message, available);
        updateSendOtpButtonState();
        return available;
    }

    // =========================
    // 4) 닉네임 blur 검증
    // =========================
    async function validateNickname({ server = true } = {}) {
        clearGlobalError();
        const v = (nicknameInput?.value || "").trim();

        if (!v) {
            state.nicknameOk = false;
            setFeedback(nicknameFeedback, nicknameInput, "닉네임을 입력해주세요.", false);
            return false;
        }
        if (v.length < NICKNAME_MIN || v.length > NICKNAME_MAX) {
            state.nicknameOk = false;
            setFeedback(nicknameFeedback, nicknameInput, "닉네임은 2~20자입니다.", false);
            return false;
        }
        if (!NICKNAME_REGEX.test(v)) {
            state.nicknameOk = false;
            setFeedback(nicknameFeedback, nicknameInput, "닉네임은 한글, 영문 소문자, 숫자만 사용할 수 있습니다.", false);
            return false;
        }

        if (!server) {
            state.nicknameOk = false;
            setFeedback(nicknameFeedback, nicknameInput, "닉네임 중복 확인이 필요합니다.", false);
            return false;
        }

        if (state.lastNicknameChecked === v && state.nicknameOk) return true;

        const seq = ++state.nicknameReqSeq;
        const { res, json } = await safeFetchJson(
            `/auth/members/check-nickname?nickname=${encodeURIComponent(v)}`,
            { method: "GET", headers: { Accept: "application/json", ...getCsrfHeader() } }
        );

        if (seq !== state.nicknameReqSeq) return false;

        if (!res || !res.ok || !json) {
            state.nicknameOk = false;
            setFeedback(nicknameFeedback, nicknameInput, json?.message ?? "요청 처리 중 오류가 발생했습니다.", false);
            return false;
        }

        const { available, message } = parseAvailability(json);
        state.nicknameOk = available;
        state.lastNicknameChecked = v;

        setFeedback(nicknameFeedback, nicknameInput, message, available);
        return available;
    }

    // =========================
    // OTP 전송/검증
    // =========================
    async function sendOtpEmail() {
        clearGlobalError();

        const emailOk = await validateEmail({ server: true });
        if (!emailOk) return;

        const email = (emailInput?.value || "").trim().toLowerCase();

        const { res, json } = await safeFetchJson(`/auth/signup/email-verification`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                Accept: "application/json",
                ...getCsrfHeader(),
            },
            body: JSON.stringify({ email }),
        });

        if (!res) {
            setFeedback(emailFeedback, emailInput, "요청 처리 중 오류가 발생했습니다.", false);
            return;
        }

        if (res.ok && json?.success) {
            setFeedback(emailFeedback, emailInput, json.message || "인증번호를 전송했습니다.", true);

            if (otpInput) {
                otpInput.disabled = false;
                otpInput.readOnly = false;
            }
            if (verifyOtpBtn) verifyOtpBtn.disabled = false;

            const otpValiditySeconds = json.data?.otpValiditySeconds ?? 0;
            const cooldownSeconds = json.data?.cooldownSeconds ?? 0;

            startOtpCountdown(otpValiditySeconds, cooldownSeconds);
            updateSendOtpButtonState();
            return;
        }

        // 400: 메시지 + 쿨다운 리셋 => 버튼 enable 보장
        if (res.status === 400) {
            setFeedback(emailFeedback, emailInput, json?.message ?? "이메일 정책 위반입니다.", false);
            stopOtpTimers();
            state.resendRemainingSec = 0;
            updateSendOtpButtonState();
            return;
        }

        // 429: Retry-After 쿨다운 표시
        if (res.status === 429) {
            setFeedback(emailFeedback, emailInput, json?.message ?? "이메일 인증 요청이 너무 많습니다.", false);

            const retryAfter = Number(res.headers.get("Retry-After") || res.headers.get("retry-after") || 0);

            stopOtpTimers();

            if (otpTimers) otpTimers.hidden = false;
            if (otpTimerSpan) otpTimerSpan.textContent = "--:--";

            state.resendRemainingSec = retryAfter;
            if (resendTimerSpan) resendTimerSpan.textContent = formatMMSS(state.resendRemainingSec);

            if (sendOtpBtn) sendOtpBtn.disabled = true;

            state.resendIntervalId = setInterval(() => {
                state.resendRemainingSec -= 1;
                if (resendTimerSpan) resendTimerSpan.textContent = formatMMSS(state.resendRemainingSec);

                if (state.resendRemainingSec <= 0) {
                    clearInterval(state.resendIntervalId);
                    state.resendIntervalId = null;
                    if (resendTimerSpan) resendTimerSpan.textContent = "00:00";
                    updateSendOtpButtonState();
                }
            }, 1000);

            return;
        }

        showGlobalError(json?.message || "요청을 처리할 수 없습니다. 잠시 후 다시 시도해주세요.");
    }

    function validateOtpOnBlur() {
        if (!otpInput || otpInput.disabled) return true;

        const v = (otpInput.value || "").trim();

        if (!v) {
            setFeedback(otpFeedback, otpInput, "인증번호를 입력해주세요.", false);
            return false;
        }
        if (!OTP_REGEX.test(v)) {
            setFeedback(otpFeedback, otpInput, "인증번호는 숫자 6자리입니다.", false);
            return false;
        }
        // 인증 번호 형식이 볼바른 경우
        setFeedback(otpFeedback, otpInput, "", true);
        return true;
    }

    async function verifyOtp() {
        clearGlobalError();

        const emailOk = await validateEmail({ server: true });
        if (!emailOk) return;

        if (!validateOtpOnBlur()) return;

        const email = (emailInput?.value || "").trim().toLowerCase();
        const otp = (otpInput?.value || "").trim();

        const { res, json } = await safeFetchJson(`/auth/signup/email-verification/verify`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                Accept: "application/json",
                ...getCsrfHeader(),
            },
            body: JSON.stringify({ email, otp }),
        });

        if (!res) {
            setFeedback(otpFeedback, otpInput, "요청 처리 중 오류가 발생했습니다.", false);
            return;
        }

        if (res.ok && json?.success) {
            // 인증 성공 시 타이머 중지 + 타이머 UI 고정
            stopOtpTimers();
            state.resendRemainingSec = 0;

            if (otpTimers) otpTimers.hidden = true;
            if (otpTimerSpan) otpTimerSpan.textContent = "--:--";
            if (resendTimerSpan) resendTimerSpan.textContent = "--:--";

            if (sendOtpBtn) sendOtpBtn.disabled = true;
            if (verifyOtpBtn) verifyOtpBtn.disabled = true;
            if (otpInput) otpInput.readOnly = true;

            state.emailVerified = true;
            state.verifiedEmail = email;

            setFeedback(otpFeedback, otpInput, json.message || "이메일 인증이 완료되었습니다.", true);
            return;
        }

        state.emailVerified = false;
        state.verifiedEmail = null;
        setFeedback(otpFeedback, otpInput, json?.message || "인증에 실패했습니다.", false);
        updateSendOtpButtonState();
    }

    // =========================
    // 이벤트 바인딩 (blur 중심 + email input에서 인증 초기화)
    // =========================
    if (usernameInput) usernameInput.addEventListener("blur", () => validateUsername({ server: true }));
    if (passwordInput) passwordInput.addEventListener("blur", () => validatePassword());
    if (nicknameInput) nicknameInput.addEventListener("blur", () => validateNickname({ server: true }));

    if (emailInput) {
        // input: 소문자 정규화 + (인증 완료 상태면) 즉시 인증 초기화 + 기존 도메인 검증결과 무효화
        emailInput.addEventListener("input", () => {
            const current = normalizeEmailInput();
            invalidateEmailCheckIfTypingChanged(current);
            resetVerificationIfEmailChanged(current);
            updateSendOtpButtonState();
        });

        // blur: 검증만 수행 (여기서 초기화 로직을 다시 돌리지 않음!)
        emailInput.addEventListener("blur", async () => {
            await validateEmail({ server: true });
        });
    }

    if (otpInput) {
        otpInput.addEventListener("input", () => {
            otpInput.value = otpInput.value.replace(/[^\d]/g, "").slice(0, 6);
        });
        otpInput.addEventListener("blur", () => validateOtpOnBlur());
    }

    // 비밀번호 토글(아이콘 클릭만)
    if (togglePasswordBtn) {
        togglePasswordBtn.addEventListener("click", () => {
            if (!passwordInput || !togglePasswordIcon) return;
            const isPw = passwordInput.type === "password";
            passwordInput.type = isPw ? "text" : "password";
            togglePasswordIcon.className = isPw ? "bi bi-eye" : "bi bi-eye-slash";
            togglePasswordBtn.setAttribute("aria-pressed", String(isPw));
        });
    }

    if (sendOtpBtn) sendOtpBtn.addEventListener("click", () => sendOtpEmail());
    if (verifyOtpBtn) verifyOtpBtn.addEventListener("click", () => verifyOtp());

    // =========================
    // submit 제어: "즉시 preventDefault" + OK일 때만 form.submit()
    // =========================
    async function runSubmitValidationAndMaybeSubmit() {
        if (submitting) return;

        clearGlobalError();

        // submit 시점에도 혹시 email 값이 바뀌었으면 인증 초기화가 반영되도록 한번 더
        if (emailInput) {
            const current = normalizeEmailInput();
            resetVerificationIfEmailChanged(current);
        }

        const u = await validateUsername({ server: true });
        const p = validatePassword();
        const em = await validateEmail({ server: true });
        const n = await validateNickname({ server: true });

        if (!state.emailVerified) {
            setFeedback(otpFeedback, otpInput, "이메일 인증이 필요합니다.", false);
        }

        const ok = u && p && em && n && state.emailVerified;

        if (!ok) {
            const firstInvalid =
                form?.querySelector(".form-control.is-invalid") ||
                (otpFeedback?.classList.contains("is-invalid") ? otpInput : null);

            firstInvalid?.focus?.();
            return;
        }

        submitting = true;
        // (선택) 전송 직전에만 disable해서 중복 제출 방지
        if (signupBtn) signupBtn.disabled = true;
        form.submit();
    }

    if (form) {
        form.addEventListener(
            "submit",
            (e) => {
                e.preventDefault();
                e.stopPropagation();
                runSubmitValidationAndMaybeSubmit();
            },
            true // 캡처 단계
        );
    }

    // =========================
    // 초기 상태
    // =========================
    function init() {
        state.emailVerified = false;
        state.verifiedEmail = null;
        state.resendRemainingSec = 0;
        resetOtpUi({ clearOtpValue: true });
        updateSendOtpButtonState();

        // 회원가입 버튼은 기본 활성(요구사항)
        if (signupBtn) signupBtn.disabled = false;
    }

    init();
})();
