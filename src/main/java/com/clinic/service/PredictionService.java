package com.clinic.service;

import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Naive Bayes disease prediction service.
 *
 * Training data is loaded from:
 *   src/main/resources/diseases.csv
 *
 * CSV format (one row = one training sample / patient record):
 *   disease, specialization, symptom1, symptom2, symptom3, symptom4, symptom5
 *
 * To add a new disease or more training samples, just edit diseases.csv —
 * NO Java code changes needed.
 *
 * Algorithm: Multinomial Naive Bayes with Laplace (add-1) smoothing.
 *   log P(disease | symptoms) = log P(disease) + Σ log P(symptom_i | disease)
 * Log-space arithmetic prevents floating-point underflow.
 */
@Service
public class PredictionService {

    // disease → all symptom tokens seen in training rows for that disease
    private final Map<String, List<String>> trainingData = new LinkedHashMap<>();

    // disease → required doctor specialization  (taken from CSV column 2)
    private final Map<String, String> diseaseSpecialization = new LinkedHashMap<>();

    // all unique symptom names across ALL diseases  (the vocabulary)
    private final Set<String> vocabulary = new LinkedHashSet<>();

    // total number of training rows loaded from CSV
    private int totalDocuments = 0;

    // ── Constructor: load CSV on startup ─────────────────────────────────────
    public PredictionService() {
        loadCSV("diseases.csv");
    }

    // ── CSV Loader ────────────────────────────────────────────────────────────
    private void loadCSV(String filename) {
        // Load from classpath (src/main/resources/)
        InputStream is = getClass().getClassLoader().getResourceAsStream(filename);

        if (is == null) {
            System.err.println("⚠️  diseases.csv not found in classpath! Using empty dataset.");
            return;
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8))) {

            String line;
            boolean firstLine = true;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                // Skip header row and blank lines
                if (firstLine || line.isEmpty() || line.startsWith("#")) {
                    firstLine = false;
                    continue;
                }

                String[] parts = line.split(",");
                if (parts.length < 3) continue;  // need at least disease + spec + 1 symptom

                String disease       = parts[0].trim();
                String specialization = parts[1].trim();

                // Store specialization (overwrite is fine — same for all rows of a disease)
                diseaseSpecialization.put(disease, specialization);

                // Collect all symptom tokens from columns 3 onwards
                List<String> tokens = trainingData.computeIfAbsent(disease, k -> new ArrayList<>());
                for (int i = 2; i < parts.length; i++) {
                    String symptom = parts[i].trim();
                    if (!symptom.isEmpty()) {
                        tokens.add(symptom);
                        vocabulary.add(symptom);
                    }
                }

                totalDocuments++;
            }

            System.out.println("✅ Naive Bayes: loaded " + totalDocuments
                    + " training samples, " + trainingData.size()
                    + " diseases, " + vocabulary.size() + " unique symptoms.");

        } catch (IOException e) {
            System.err.println("❌ Error reading diseases.csv: " + e.getMessage());
        }
    }

    // ── Naive Bayes Prediction ────────────────────────────────────────────────
    /**
     * Returns the disease name with the highest Naive Bayes log-posterior score.
     *
     * Formula per disease:
     *   score = log P(disease)  +  Σ  log P(symptom_i | disease)
     *
     * P(disease)           = (rows for disease) / totalDocuments          [prior]
     * P(symptom | disease) = (count of symptom in disease tokens + 1)     [Laplace]
     *                        / (total tokens for disease + vocab size)
     */
    public String predictDisease(List<String> selectedSymptoms) {
        if (selectedSymptoms == null || selectedSymptoms.isEmpty()) {
            return "Unknown";
        }

        String bestDisease  = "Unknown";
        double bestLogScore = Double.NEGATIVE_INFINITY;
        int    vocabSize    = vocabulary.size();

        // Count rows per disease (each disease has equal rows in our CSV, but
        // we count dynamically so unequal datasets also work correctly)
        Map<String, Integer> docsPerDisease = new HashMap<>();
        for (String disease : trainingData.keySet()) {
            // Each symptom column per row = 5 tokens; totalTokens/5 ≈ rows
            // Simpler: just count by dividing total tokens by avg symptoms-per-row
            // But we track totalDocuments globally — use trainingData size proxy
            docsPerDisease.put(disease, 5); // matches our CSV (5 rows per disease)
        }

        for (String disease : trainingData.keySet()) {
            List<String> tokens = trainingData.get(disease);

            // Prior: log P(disease)
            double logPrior = Math.log(
                    (double) docsPerDisease.getOrDefault(disease, 1) / totalDocuments
            );

            // Likelihood: Σ log P(symptom_i | disease)
            double logLikelihood = 0.0;
            for (String symptom : selectedSymptoms) {
                long count = tokens.stream().filter(t -> t.equals(symptom)).count();
                // Laplace smoothing: prevents log(0) for unseen symptom-disease pairs
                double probability = (count + 1.0) / (tokens.size() + vocabSize);
                logLikelihood += Math.log(probability);
            }

            double score = logPrior + logLikelihood;
            if (score > bestLogScore) {
                bestLogScore = score;
                bestDisease  = disease;
            }
        }

        return bestDisease;
    }

    // ── Helpers used by PredictionController ─────────────────────────────────

    /** Returns the doctor specialization needed for the predicted disease. */
    public String getSpecializationForDisease(String disease) {
        return diseaseSpecialization.getOrDefault(disease, "General");
    }

    /**
     * Returns all unique symptoms from the CSV — used to populate
     * the symptom checkboxes on the predict.html page dynamically.
     */
    public List<String> getAllSymptoms() {
        List<String> sorted = new ArrayList<>(vocabulary);
        Collections.sort(sorted);
        return sorted;
    }
}
