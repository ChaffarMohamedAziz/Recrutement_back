package com.recrutement.recrutement.service;

import com.recrutement.recrutement.dto.*;
import java.util.List;

public interface AuthService {
    RegisterResponse registerCandidate(CandidateRegisterRequest request);

    RegisterResponse registerRecruiter(RecruiterRegisterRequest request);

    RegisterResponse registerAdmin(AdminRegisterRequest request);

    LoginResponse login(LoginRequest request);

    SocialAuthResponse socialAuth(SocialAuthRequest request);

    String activateAccount(String token);

    RegisterResponse approveRecruiter(Long recruiterId);

    MessageResponse rejectRecruiter(Long recruiterId);

    List<RegisterResponse> getRecruiterAccounts();

    void deleteRecruiterAccount(Long recruiterId);

    List<UserSummaryResponse> getUsers(String query);

    UserProfileResponse getUserById(Long userId);

    MessageResponse deleteUser(Long userId);

    MessageResponse forgotPassword(ForgotPasswordRequest request);

    MessageResponse resetPassword(ResetPasswordRequest request);
}
