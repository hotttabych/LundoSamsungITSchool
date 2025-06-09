package io.whyscape.lundo.domain.model;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.Objects;

public class Question {
    private final String text;
    private final List<String> answers;
    private final String correctAnswer;
    private final String explanation;

    public Question(String text, List<String> answers, String correctAnswer, String explanation) {
        this.text = text;
        this.answers = answers;
        this.correctAnswer = correctAnswer;
        this.explanation = explanation;
    }

    public String getText() {
        return text;
    }

    public List<String> getAnswers() {
        return answers;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public String getExplanation() {
        return explanation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Question)) return false;
        Question question = (Question) o;
        return Objects.equals(text, question.text) &&
                Objects.equals(answers, question.answers) &&
                Objects.equals(correctAnswer, question.correctAnswer) &&
                Objects.equals(explanation, question.explanation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, answers, correctAnswer, explanation);
    }

    @NonNull
    @Override
    public String toString() {
        return "Question{" +
                "text='" + text + '\'' +
                ", answers=" + answers +
                ", correctAnswer='" + correctAnswer + '\'' +
                ", explanation='" + explanation + '\'' +
                '}';
    }
}