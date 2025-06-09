package io.whyscape.lundo.domain.model;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.Objects;

public class TestData {
    private final String title;
    private final List<Question> questions;

    public TestData(String title, List<Question> questions) {
        this.title = title;
        this.questions = questions;
    }

    public String getTitle() {
        return title;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TestData)) return false;
        TestData testData = (TestData) o;
        return Objects.equals(title, testData.title) &&
                Objects.equals(questions, testData.questions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, questions);
    }

    @NonNull
    @Override
    public String toString() {
        return "TestData{" +
                "title='" + title + '\'' +
                ", questions=" + questions +
                '}';
    }
}