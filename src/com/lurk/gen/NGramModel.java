package com.lurk.gen;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NGramModel {

    private final Map<List<String>, List<String>> ngrams = new HashMap<>();
    private final Map<List<String>, Integer> ngramsCounter = new HashMap<>();
    private final int n;

    public NGramModel(int n) {
        this.n = n;
    }

    public void learn(String text) {
        getNGrams(text)
                .forEach(ngram -> {
                    ngramsCounter.put(ngram, ngramsCounter.computeIfAbsent(ngram, ng -> 0) + 1);
                    String word = ngram.get(n - 1);
                    List<String> context = new ArrayList<>(ngram);
                    context.remove(n - 1);
                    ngrams.computeIfAbsent(context, ctx -> new ArrayList<>()).add(word);
                });
    }

    public String generateText(String start, int length) {
        StringBuilder builder = new StringBuilder();
        builder.append(start).append(' ');
        List<List<String>> ngrams = getNGrams(start);
        List<String> context = ngrams.get(ngrams.size() - 1);
        for (int i = 0; i < length; ++i) {
            try {
                String nextWord = getNextWord(context);
                builder.append(nextWord).append(' ');
                context.remove(0);
                context.add(nextWord);
            } catch (IllegalStateException error) {
                builder.append(generateText(length - i));
                break;
            }
        }
        return builder.toString();
    }

    public String generateText(int length) {
        StringBuilder builder = new StringBuilder();
        List<String> context = getStartNGram();
        for (int i = 0; i < length; ++i) {
            try {
                String nextWord = getNextWord(context);
                builder.append(nextWord).append(' ');
                context.remove(0);
                context.add(nextWord);
            } catch (IllegalStateException error) {
                builder.append(generateText(length - i));
                break;
            }
        }
        return builder.toString();
    }

    public void dump() {
        ngrams.forEach((ngram, words) -> {
            StringBuilder builder = new StringBuilder();
            ngram.forEach(wrd -> builder.append(wrd).append(' '));
            builder.append(": ");
            words.forEach(wrd -> builder.append(wrd).append(", "));
            System.out.println(builder);
        });
    }

    private String getNextWord(List<String> context) {
        List<String> probableWords = ngrams.get(context);
        if (probableWords == null) {
            throw new IllegalStateException("Reached a dead end context");
        }
        List<Float> probs = probableWords.stream()
                .map(word -> getProbability(context, word))
                .collect(Collectors.toList());
        float wholeSum = probs.stream().reduce(0f, Float::sum);
        float rand = (float) Math.random() * wholeSum;

        float sum = 0f;
        for (int i = 0; i < probs.size(); ++i) {
            sum += probs.get(i);
            if (sum >= rand) {
                return probableWords.get(i);
            }
        }
        return probableWords.get(0);
    }

    private float getProbability(List<String> context, String word) {
        List<String> ngram = new ArrayList<>(context);
        ngram.add(word);

        float ngramCount = ngramsCounter.get(ngram);
        float optionsNum = ngrams.get(context).size();
        return ngramCount / optionsNum;
    }

    private List<String> getStartNGram() {
        List<String> ngram = new ArrayList<>();
        for (int i = 0; i < n - 1; ++i) {
            ngram.add("START");
        }
        return ngram;
    }

    private List<List<String>> getNGrams(String text) {
        List<List<String>> ngrams = new ArrayList<>();
        text = "START ".repeat(n - 1) + text;

        List<String> tokens = tokenize(text);
        for (int i = 0; i < tokens.size() - n; ++i) {
            List<String> ngram = new ArrayList<>();
            for (int j = 0; j < n; ++j) {
                ngram.add(tokens.get(i + j));
            }
            ngrams.add(ngram);
        }
        return ngrams;
    }

    private List<String> tokenize(String text) {
        String spaced = text.replaceAll("\\p{Punct}", " $0 ");
        return Stream.of(spaced.split("(?U)\\s")).filter(token -> !token.isEmpty()).collect(Collectors.toList());
    }

}
