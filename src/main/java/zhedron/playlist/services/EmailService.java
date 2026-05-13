package zhedron.playlist.services;

public interface EmailService {
    void sendTo(String to, String subject, String text);
}
