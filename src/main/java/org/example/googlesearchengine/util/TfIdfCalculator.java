package org.example.googlesearchengine.util;

import org.springframework.stereotype.Component;

@Component
public class TfIdfCalculator {

    public double calculateTf(int termFrequency, int totalWords) {
        if (totalWords == 0) return 0;
        return termFrequency / (double) totalWords;
    }

    public double calculateIdf(long totalDocuments, long docsContainingTerm) {
        if (docsContainingTerm == 0) return 0;
        return Math.log(totalDocuments / (double) docsContainingTerm);
    }

    public double calculateTfIdf(int termFrequency, int totalWords,
                                 long totalDocuments, long docsContainingTerm) {
        return calculateTf(termFrequency, totalWords)
                * calculateIdf(totalDocuments, docsContainingTerm);
    }
}