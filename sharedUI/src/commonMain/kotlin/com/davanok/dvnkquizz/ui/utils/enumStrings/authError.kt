package com.davanok.dvnkquizz.ui.utils.enumStrings

import com.davanok.dvnkquizz.core.domain.auth.enums.UserAuthErrorCode
import dvnkquizz.sharedui.generated.resources.Res
import dvnkquizz.sharedui.generated.resources.auth_error_anonymous_provider_disabled
import dvnkquizz.sharedui.generated.resources.auth_error_bad_code_verifier
import dvnkquizz.sharedui.generated.resources.auth_error_bad_jwt
import dvnkquizz.sharedui.generated.resources.auth_error_bad_oauth_callback
import dvnkquizz.sharedui.generated.resources.auth_error_bad_oauth_state
import dvnkquizz.sharedui.generated.resources.auth_error_captcha_failed
import dvnkquizz.sharedui.generated.resources.auth_error_email_address_invalid
import dvnkquizz.sharedui.generated.resources.auth_error_email_address_not_authorized
import dvnkquizz.sharedui.generated.resources.auth_error_email_conflict_identity_not_deletable
import dvnkquizz.sharedui.generated.resources.auth_error_email_exists
import dvnkquizz.sharedui.generated.resources.auth_error_email_not_confirmed
import dvnkquizz.sharedui.generated.resources.auth_error_email_provider_disabled
import dvnkquizz.sharedui.generated.resources.auth_error_flow_state_expired
import dvnkquizz.sharedui.generated.resources.auth_error_flow_state_not_found
import dvnkquizz.sharedui.generated.resources.auth_error_identity_already_exists
import dvnkquizz.sharedui.generated.resources.auth_error_identity_not_found
import dvnkquizz.sharedui.generated.resources.auth_error_insufficient_aal
import dvnkquizz.sharedui.generated.resources.auth_error_invalid_credentials
import dvnkquizz.sharedui.generated.resources.auth_error_invite_not_found
import dvnkquizz.sharedui.generated.resources.auth_error_manual_linking_disabled
import dvnkquizz.sharedui.generated.resources.auth_error_mfa_challenge_expired
import dvnkquizz.sharedui.generated.resources.auth_error_mfa_factor_name_conflict
import dvnkquizz.sharedui.generated.resources.auth_error_mfa_factor_not_found
import dvnkquizz.sharedui.generated.resources.auth_error_mfa_ip_address_mismatch
import dvnkquizz.sharedui.generated.resources.auth_error_mfa_phone_enroll_disabled
import dvnkquizz.sharedui.generated.resources.auth_error_mfa_phone_verify_disabled
import dvnkquizz.sharedui.generated.resources.auth_error_mfa_totp_enroll_disabled
import dvnkquizz.sharedui.generated.resources.auth_error_mfa_totp_verify_disabled
import dvnkquizz.sharedui.generated.resources.auth_error_mfa_verification_failed
import dvnkquizz.sharedui.generated.resources.auth_error_mfa_verification_rejected
import dvnkquizz.sharedui.generated.resources.auth_error_mfa_verified_factor_exists
import dvnkquizz.sharedui.generated.resources.auth_error_mfa_webauthn_enroll_disabled
import dvnkquizz.sharedui.generated.resources.auth_error_mfa_webauthn_verify_disabled
import dvnkquizz.sharedui.generated.resources.auth_error_oauth_provider_not_supported
import dvnkquizz.sharedui.generated.resources.auth_error_otp_disabled
import dvnkquizz.sharedui.generated.resources.auth_error_otp_expired
import dvnkquizz.sharedui.generated.resources.auth_error_otp_provider_disabled
import dvnkquizz.sharedui.generated.resources.auth_error_over_email_send_rate_limit
import dvnkquizz.sharedui.generated.resources.auth_error_over_request_rate_limit
import dvnkquizz.sharedui.generated.resources.auth_error_over_sms_send_rate_limit
import dvnkquizz.sharedui.generated.resources.auth_error_phone_exists
import dvnkquizz.sharedui.generated.resources.auth_error_phone_not_confirmed
import dvnkquizz.sharedui.generated.resources.auth_error_phone_provider_disabled
import dvnkquizz.sharedui.generated.resources.auth_error_provider_disabled
import dvnkquizz.sharedui.generated.resources.auth_error_provider_email_needs_verification
import dvnkquizz.sharedui.generated.resources.auth_error_reauth_nonce_missing
import dvnkquizz.sharedui.generated.resources.auth_error_reauthentication_needed
import dvnkquizz.sharedui.generated.resources.auth_error_reauthentication_not_valid
import dvnkquizz.sharedui.generated.resources.auth_error_refresh_token_already_used
import dvnkquizz.sharedui.generated.resources.auth_error_refresh_token_not_found
import dvnkquizz.sharedui.generated.resources.auth_error_same_password
import dvnkquizz.sharedui.generated.resources.auth_error_saml_assertion_no_email
import dvnkquizz.sharedui.generated.resources.auth_error_saml_assertion_no_user_id
import dvnkquizz.sharedui.generated.resources.auth_error_saml_entity_id_mismatch
import dvnkquizz.sharedui.generated.resources.auth_error_saml_provider_disabled
import dvnkquizz.sharedui.generated.resources.auth_error_saml_relay_state_expired
import dvnkquizz.sharedui.generated.resources.auth_error_saml_relay_state_not_found
import dvnkquizz.sharedui.generated.resources.auth_error_session_expired
import dvnkquizz.sharedui.generated.resources.auth_error_session_not_found
import dvnkquizz.sharedui.generated.resources.auth_error_signup_disabled
import dvnkquizz.sharedui.generated.resources.auth_error_single_identity_not_deletable
import dvnkquizz.sharedui.generated.resources.auth_error_too_many_enrolled_mfa_factors
import dvnkquizz.sharedui.generated.resources.auth_error_unexpected_failure
import dvnkquizz.sharedui.generated.resources.auth_error_user_already_exists
import dvnkquizz.sharedui.generated.resources.auth_error_user_banned
import dvnkquizz.sharedui.generated.resources.auth_error_user_not_found
import dvnkquizz.sharedui.generated.resources.auth_error_user_sso_managed
import dvnkquizz.sharedui.generated.resources.auth_error_validation_failed
import dvnkquizz.sharedui.generated.resources.auth_error_weak_password
import dvnkquizz.sharedui.generated.resources.auth_error_web3_provider_disabled
import dvnkquizz.sharedui.generated.resources.auth_error_web3_unsupported_chain
import org.jetbrains.compose.resources.StringResource

fun UserAuthErrorCode.stringRes(): StringResource = when (this) {
    UserAuthErrorCode.InvalidCredentials              -> Res.string.auth_error_invalid_credentials
    UserAuthErrorCode.WeakPassword                    -> Res.string.auth_error_weak_password
    UserAuthErrorCode.SamePassword                    -> Res.string.auth_error_same_password
    UserAuthErrorCode.EmailExists                     -> Res.string.auth_error_email_exists
    UserAuthErrorCode.PhoneExists                     -> Res.string.auth_error_phone_exists
    UserAuthErrorCode.UserAlreadyExists               -> Res.string.auth_error_user_already_exists
    UserAuthErrorCode.IdentityAlreadyExists           -> Res.string.auth_error_identity_already_exists
    UserAuthErrorCode.IdentityNotFound                -> Res.string.auth_error_identity_not_found
    UserAuthErrorCode.SingleIdentityNotDeletable      -> Res.string.auth_error_single_identity_not_deletable
    UserAuthErrorCode.EmailConflictIdentityNotDeletable -> Res.string.auth_error_email_conflict_identity_not_deletable
    UserAuthErrorCode.UserNotFound                    -> Res.string.auth_error_user_not_found
    UserAuthErrorCode.UserBanned                      -> Res.string.auth_error_user_banned
    UserAuthErrorCode.UserSsoManaged                  -> Res.string.auth_error_user_sso_managed
    UserAuthErrorCode.EmailNotConfirmed               -> Res.string.auth_error_email_not_confirmed
    UserAuthErrorCode.PhoneNotConfirmed               -> Res.string.auth_error_phone_not_confirmed
    UserAuthErrorCode.ProviderEmailNeedsVerification  -> Res.string.auth_error_provider_email_needs_verification
    UserAuthErrorCode.EmailAddressInvalid             -> Res.string.auth_error_email_address_invalid
    UserAuthErrorCode.EmailAddressNotAuthorized       -> Res.string.auth_error_email_address_not_authorized
    UserAuthErrorCode.OtpExpired                      -> Res.string.auth_error_otp_expired
    UserAuthErrorCode.OtpDisabled                     -> Res.string.auth_error_otp_disabled
    UserAuthErrorCode.BadCodeVerifier                 -> Res.string.auth_error_bad_code_verifier
    UserAuthErrorCode.BadJwt                          -> Res.string.auth_error_bad_jwt
    UserAuthErrorCode.RefreshTokenNotFound            -> Res.string.auth_error_refresh_token_not_found
    UserAuthErrorCode.RefreshTokenAlreadyUsed         -> Res.string.auth_error_refresh_token_already_used
    UserAuthErrorCode.SessionNotFound                 -> Res.string.auth_error_session_not_found
    UserAuthErrorCode.SessionExpired                  -> Res.string.auth_error_session_expired
    UserAuthErrorCode.FlowStateNotFound               -> Res.string.auth_error_flow_state_not_found
    UserAuthErrorCode.FlowStateExpired                -> Res.string.auth_error_flow_state_expired
    UserAuthErrorCode.MfaVerificationFailed           -> Res.string.auth_error_mfa_verification_failed
    UserAuthErrorCode.MfaVerificationRejected         -> Res.string.auth_error_mfa_verification_rejected
    UserAuthErrorCode.MfaChallengeExpired             -> Res.string.auth_error_mfa_challenge_expired
    UserAuthErrorCode.MfaIpAddressMismatch            -> Res.string.auth_error_mfa_ip_address_mismatch
    UserAuthErrorCode.MfaFactorNotFound               -> Res.string.auth_error_mfa_factor_not_found
    UserAuthErrorCode.MfaFactorNameConflict           -> Res.string.auth_error_mfa_factor_name_conflict
    UserAuthErrorCode.TooManyEnrolledMfaFactors       -> Res.string.auth_error_too_many_enrolled_mfa_factors
    UserAuthErrorCode.MfaVerifiedFactorExists         -> Res.string.auth_error_mfa_verified_factor_exists
    UserAuthErrorCode.ReauthenticationNeeded          -> Res.string.auth_error_reauthentication_needed
    UserAuthErrorCode.ReauthenticationNotValid        -> Res.string.auth_error_reauthentication_not_valid
    UserAuthErrorCode.ReauthNonceMissing              -> Res.string.auth_error_reauth_nonce_missing
    UserAuthErrorCode.InsufficientAal                 -> Res.string.auth_error_insufficient_aal
    UserAuthErrorCode.BadOauthState                   -> Res.string.auth_error_bad_oauth_state
    UserAuthErrorCode.BadOauthCallback                -> Res.string.auth_error_bad_oauth_callback
    UserAuthErrorCode.SamlRelayStateNotFound          -> Res.string.auth_error_saml_relay_state_not_found
    UserAuthErrorCode.SamlRelayStateExpired           -> Res.string.auth_error_saml_relay_state_expired
    UserAuthErrorCode.SamlAssertionNoUserId           -> Res.string.auth_error_saml_assertion_no_user_id
    UserAuthErrorCode.SamlAssertionNoEmail            -> Res.string.auth_error_saml_assertion_no_email
    UserAuthErrorCode.SamlEntityIdMismatch            -> Res.string.auth_error_saml_entity_id_mismatch
    UserAuthErrorCode.OverRequestRateLimit            -> Res.string.auth_error_over_request_rate_limit
    UserAuthErrorCode.OverEmailSendRateLimit          -> Res.string.auth_error_over_email_send_rate_limit
    UserAuthErrorCode.OverSmsSendRateLimit            -> Res.string.auth_error_over_sms_send_rate_limit
    UserAuthErrorCode.SignupDisabled                  -> Res.string.auth_error_signup_disabled
    UserAuthErrorCode.EmailProviderDisabled           -> Res.string.auth_error_email_provider_disabled
    UserAuthErrorCode.PhoneProviderDisabled           -> Res.string.auth_error_phone_provider_disabled
    UserAuthErrorCode.OauthProviderNotSupported       -> Res.string.auth_error_oauth_provider_not_supported
    UserAuthErrorCode.ProviderDisabled                -> Res.string.auth_error_provider_disabled
    UserAuthErrorCode.AnonymousProviderDisabled       -> Res.string.auth_error_anonymous_provider_disabled
    UserAuthErrorCode.SamlProviderDisabled            -> Res.string.auth_error_saml_provider_disabled
    UserAuthErrorCode.MfaPhoneEnrollDisabled          -> Res.string.auth_error_mfa_phone_enroll_disabled
    UserAuthErrorCode.MfaPhoneVerifyDisabled          -> Res.string.auth_error_mfa_phone_verify_disabled
    UserAuthErrorCode.MfaTotpEnrollDisabled           -> Res.string.auth_error_mfa_totp_enroll_disabled
    UserAuthErrorCode.MfaTotpVerifyDisabled           -> Res.string.auth_error_mfa_totp_verify_disabled
    UserAuthErrorCode.MfaWebAuthnEnrollDisabled       -> Res.string.auth_error_mfa_webauthn_enroll_disabled
    UserAuthErrorCode.MfaWebAuthnVerifyDisabled       -> Res.string.auth_error_mfa_webauthn_verify_disabled
    UserAuthErrorCode.ManualLinkingDisabled           -> Res.string.auth_error_manual_linking_disabled
    UserAuthErrorCode.Web3ProviderDisabled            -> Res.string.auth_error_web3_provider_disabled
    UserAuthErrorCode.Web3UnsupportedChain            -> Res.string.auth_error_web3_unsupported_chain
    UserAuthErrorCode.InviteNotFound                  -> Res.string.auth_error_invite_not_found
    UserAuthErrorCode.CaptchaFailed                   -> Res.string.auth_error_captcha_failed
    UserAuthErrorCode.ValidationFailed                -> Res.string.auth_error_validation_failed
    UserAuthErrorCode.UnexpectedFailure               -> Res.string.auth_error_unexpected_failure
    UserAuthErrorCode.OtpDisabledForProvider          -> Res.string.auth_error_otp_provider_disabled
}