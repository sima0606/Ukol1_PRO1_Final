package pro1;

import  java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) {
        Path inputDir = Paths.get(System.getProperty("user.dir"), "input");
        Path outputDir = Paths.get(System.getProperty("user.dir"), "output");

        try {
            if (!Files.exists(outputDir)) {
                Files.createDirectory(outputDir);
            }

            try (Stream<Path> paths = Files.list(inputDir)) {
                List<Path> csvFiles = paths
                        .filter(f -> f.toString().endsWith(".csv"))
                        .collect(Collectors.toList());

                for (Path file : csvFiles) {
                    processFile(file, outputDir);
                }
            }
        } catch (IOException e) {
            System.err.println("Kritická chyba: " + e.getMessage());
        }
    }

    private static void processFile(Path inputFile, Path outputDir) {
        try {
            List<String> lines;
            try {
                lines = Files.readAllLines(inputFile, StandardCharsets.UTF_8);
            } catch (Exception e) {
                lines = Files.readAllLines(inputFile, StandardCharsets.ISO_8859_1);
            }

            List<String> processedLines = new ArrayList<>();

            for (String line : lines) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split("[;=:,]", 2);
                if (parts.length < 2) continue;

                String name = parts[0].trim();
                String mathExpression = parts[1].trim();
                String[] terms = mathExpression.split("\\+");

                long totalNum = 0;
                long totalDen = 1;

                try {
                    for (String term : terms) {
                        term = term.trim();
                        long n, d;

                        if (term.endsWith("%")) {
                            n = Long.parseLong(term.replace("%", "").trim());
                            d = 100;
                        } else if (term.contains("/")) {
                            String[] fractionParts = term.split("/");
                            n = Long.parseLong(fractionParts[0].trim());
                            d = Long.parseLong(fractionParts[1].trim());
                        } else {
                            // Pro případ celých čísel
                            n = Long.parseLong(term);
                            d = 1;
                        }

                        // Matematika
                        long newNum = totalNum * d + n * totalDen;
                        long newDen = totalDen * d;

                        // Krácení
                        long gcd = gcd(Math.abs(newNum), Math.abs(newDen));
                        totalNum = newNum / gcd;
                        totalDen = newDen / gcd;
                    }

                    // Zápis výsledku
                    processedLines.add(name + "," + totalNum + "/" + totalDen);
                } catch (Exception e) {
                    System.err.println("Přeskočen nečitelný výraz na řádku: " + line);
                }
            }

            Path outputFile = outputDir.resolve(inputFile.getFileName());
            Files.write(outputFile, processedLines);
            // System.out.println("Úspěšně zpracován soubor: " + inputFile.getFileName());
        } catch (IOException e) {
            System.err.println("Nepodařilo se zpracovat soubor: " + inputFile.getFileName());
        }
    }

    private static long gcd(long a, long b) {
        while (b != 0) {
            long temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }
}