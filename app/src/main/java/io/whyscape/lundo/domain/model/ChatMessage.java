package io.whyscape.lundo.domain.model;

public class ChatMessage {
    private final String role;
    private final String text;
    private final String fileName;
    private final boolean successful;

    public ChatMessage(String role, String text, String fileName, boolean successful) {
        this.role = role;
        this.text = text;
        this.fileName = fileName;
        this.successful = successful;
    }

    public ChatMessage(String role, String text) {
        this(role, text, null, true);
    }

    public String getRole() {
        return role;
    }

    public String getText() {
        return text;
    }

    public String getFileName() {
        return fileName;
    }

    public boolean isSuccessful() {
        return successful;
    }
}