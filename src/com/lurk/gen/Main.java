package com.lurk.gen;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {

    public static void main(String[] args) {
        NGramModel model = new NGramModel(3);
        learnOnFileContents("lurk.txt", model);
        writeToFile(model.generateText(400));
        System.out.println("Done");
    }

    private static void learnOnFileContents(String path, NGramModel model) {
        List<StringBuilder> fiveHundredLinesEach = new ArrayList<>();
        fiveHundredLinesEach.add(new StringBuilder());
        AtomicInteger counter = new AtomicInteger(0);
        try {
            Path file = Paths.get(path);
            Files.lines(file).forEach(line -> {
                int cntr = counter.incrementAndGet();
                if (cntr % 500 == 0) {
                    fiveHundredLinesEach.add(new StringBuilder());
                }
                fiveHundredLinesEach.get(fiveHundredLinesEach.size()-1).append(line);
            });
            fiveHundredLinesEach.forEach(builder -> model.learn(builder.toString()));
        } catch (IOException error) {
            System.out.println(error.getMessage());
        }
    }

    private static void writeToFile(String text) {
        try {
            Path file  = Paths.get("result.txt");
            Files.write(file, text.getBytes(StandardCharsets.UTF_8));
        } catch (IOException error) {
            System.out.println(error.getMessage());
        }
    }

}
