package com.davanok.dvnkquizz.core.domain.auth.enums

import io.github.jan.supabase.auth.exception.AuthErrorCode

enum class UserAuthErrorCode(val value: String) {
    UnexpectedFailure("unexpected_failure"),
    // --- Credentials & Identity ---
    InvalidCredentials("invalid_credentials"),
    WeakPassword("weak_password"),
    SamePassword("same_password"),
    EmailExists("email_exists"),
    PhoneExists("phone_exists"),
    UserAlreadyExists("user_already_exists"),
    IdentityAlreadyExists("identity_already_exists"),
    IdentityNotFound("identity_not_found"),
    SingleIdentityNotDeletable("single_identity_not_deletable"),
    EmailConflictIdentityNotDeletable("email_conflict_identity_not_deletable"),
    UserNotFound("user_not_found"),
    UserBanned("user_banned"),
    UserSsoManaged("user_sso_managed"),

    // --- Email & Phone Confirmation ---
    EmailNotConfirmed("email_not_confirmed"),
    PhoneNotConfirmed("phone_not_confirmed"),
    ProviderEmailNeedsVerification("provider_email_needs_verification"),
    EmailAddressInvalid("email_address_invalid"),
    EmailAddressNotAuthorized("email_address_not_authorized"),

    // --- OTP / Token / Code ---
    OtpExpired("otp_expired"),
    OtpDisabled("otp_disabled"),
    BadCodeVerifier("bad_code_verifier"),
    BadJwt("bad_jwt"),
    RefreshTokenNotFound("refresh_token_not_found"),
    RefreshTokenAlreadyUsed("refresh_token_already_used"),

    // --- Session & Flow ---
    SessionNotFound("session_not_found"),
    SessionExpired("session_expired"),
    FlowStateNotFound("flow_state_not_found"),
    FlowStateExpired("flow_state_expired"),

    // --- MFA ---
    MfaVerificationFailed("mfa_verification_failed"),
    MfaVerificationRejected("mfa_verification_rejected"),
    MfaChallengeExpired("mfa_challenge_expired"),
    MfaIpAddressMismatch("mfa_ip_address_mismatch"),
    MfaFactorNotFound("mfa_factor_not_found"),
    MfaFactorNameConflict("mfa_factor_name_conflict"),
    TooManyEnrolledMfaFactors("too_many_enrolled_mfa_factors"),
    MfaVerifiedFactorExists("mfa_verified_factor_exists"),

    // --- Reauthentication ---
    ReauthenticationNeeded("reauthentication_needed"),
    ReauthenticationNotValid("reauthentication_not_valid"),
    ReauthNonceMissing("reauth_nonce_missing"),
    InsufficientAal("insufficient_aal"),

    // --- OAuth & SSO ---
    BadOauthState("bad_oauth_state"),
    BadOauthCallback("bad_oauth_callback"),
    SamlRelayStateNotFound("saml_relay_state_not_found"),
    SamlRelayStateExpired("saml_relay_state_expired"),
    SamlAssertionNoUserId("saml_assertion_no_user_id"),
    SamlAssertionNoEmail("saml_assertion_no_email"),
    SamlEntityIdMismatch("saml_entity_id_mismatch"),

    // --- Rate Limits ---
    OverRequestRateLimit("over_request_rate_limit"),
    OverEmailSendRateLimit("over_email_send_rate_limit"),
    OverSmsSendRateLimit("over_sms_send_rate_limit"),

    // --- Disabled Features (user attempted a disabled action) ---
    SignupDisabled("signup_disabled"),
    EmailProviderDisabled("email_provider_disabled"),
    PhoneProviderDisabled("phone_provider_disabled"),
    OauthProviderNotSupported("oauth_provider_not_supported"),
    ProviderDisabled("provider_disabled"),
    OtpDisabledForProvider("otp_disabled"),
    AnonymousProviderDisabled("anonymous_provider_disabled"),
    SamlProviderDisabled("saml_provider_disabled"),
    MfaPhoneEnrollDisabled("mfa_phone_enroll_not_enabled"),
    MfaPhoneVerifyDisabled("mfa_phone_verify_not_enabled"),
    MfaTotpEnrollDisabled("mfa_totp_enroll_not_enabled"),
    MfaTotpVerifyDisabled("mfa_totp_verify_not_enabled"),
    MfaWebAuthnEnrollDisabled("mfa_webauthn_enroll_not_enabled"),
    MfaWebAuthnVerifyDisabled("mfa_webauthn_verify_not_enabled"),
    ManualLinkingDisabled("manual_linking_disabled"),
    Web3ProviderDisabled("web3_provider_disabled"),
    Web3UnsupportedChain("web3_unsupported_chain"),

    // --- Misc User-Triggered ---
    InviteNotFound("invite_not_found"),
    CaptchaFailed("captcha_failed"),
    ValidationFailed("validation_failed");

    companion object {
        fun fromValue(value: String): UserAuthErrorCode? =
            entries.firstOrNull { it.value == value }

        /**
         * Maps an [io.github.jan.supabase.auth.exception.AuthErrorCode] to a [UserAuthErrorCode].
         * Returns null if the error is not user-caused (e.g. server/system errors).
         */
        internal fun fromAuthErrorCode(authErrorCode: AuthErrorCode): UserAuthErrorCode? =
            fromValue(authErrorCode.value)
    }
}