package com.application.bghit.services;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.rest.verify.v2.service.Verification;
import com.twilio.rest.verify.v2.service.VerificationCheck;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TwilioSmsSenderService {
    private final String VERIFY_SERVICE_SID = "VAfd1d8ca321a46e359b8f43bba195462d";

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.phone.number}")
    private String fromPhoneNumber;

    @PostConstruct
    public void init() {
        Twilio.init(accountSid, authToken);
    }

    public void sendSms(String to, String message) {
        Message.creator(
                new PhoneNumber(to),
                new PhoneNumber(fromPhoneNumber),
                message).create();
    }
    // Méthode pour démarrer la vérification
    public void startVerification(String phoneNumber) {
        Verification verification = Verification.creator(VERIFY_SERVICE_SID, phoneNumber, "sms").create();
    }

    // Méthode pour vérifier le code
    public boolean verifyCode(String from, String code) {
        VerificationCheck verificationCheck = VerificationCheck.creator(VERIFY_SERVICE_SID, code)
                .setTo(from)
                .create();
        return "approved".equals(verificationCheck.getStatus());
    }

}
