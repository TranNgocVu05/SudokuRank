package ntu.tranngocvu.sudokurank.utils;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class SudokuSeeder {

    public interface SeedCallback {
        void onSuccess();
        void onError(Exception e);
    }

    private static int pendingWrites = 0;
    private static boolean hasError = false;

    private static final String[] SOLUTIONS = {
            "534678912672195348198342567859761423426853791713924856961537284287419635345286179",
            "417369825632158947958724316825437169791586432346912758289643571573291684164875293",
            "145327698839654127672918543496185372218473956753296814381562479927841365564739281",
            "295743861431865927876192543387459216612387495549216738763524189928671354154938672",
            "462831957795426183381795426173984265659312748248657319836549271924178536517263894"
    };

    public static void seedAllLevels(SeedCallback callback) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        pendingWrites = 300;
        hasError = false;

        seedDifficulty(db, "Easy", 35, 1, callback);
        seedDifficulty(db, "Medium", 45, 101, callback);
        seedDifficulty(db, "Hard", 55, 201, callback);
    }

    private static void seedDifficulty(FirebaseFirestore db,
                                       String difficulty,
                                       int emptyCount,
                                       int startLevel,
                                       SeedCallback callback) {

        for (int i = 1; i <= 100; i++) {

            long levelNumber = i;
            String solution = randomizeSolution(SOLUTIONS[i % SOLUTIONS.length], i);
            String puzzle = makePuzzle(solution, emptyCount, i);

            Map<String, Object> data = new HashMap<>();
            data.put("level", levelNumber);
            data.put("difficulty", difficulty);
            data.put("puzzle", puzzle);
            data.put("solution", solution);

            String docId = difficulty.toLowerCase() + "_" + i;

            db.collection("sudoku_levels")
                    .document(docId)
                    .set(data)
                    .addOnSuccessListener(unused -> {
                        pendingWrites--;

                        if (pendingWrites == 0 && !hasError && callback != null) {
                            callback.onSuccess();
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (!hasError) {
                            hasError = true;

                            if (callback != null) {
                                callback.onError(e);
                            }
                        }
                    });
        }
    }

    private static String randomizeSolution(String base, int seed) {

        Random random = new Random(seed);

        char[] map = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

        for (int i = 1; i <= 9; i++) {
            int j = 1 + random.nextInt(9);

            char temp = map[i];
            map[i] = map[j];
            map[j] = temp;
        }

        StringBuilder result = new StringBuilder();

        for (int i = 0; i < base.length(); i++) {
            char ch = base.charAt(i);
            result.append(map[ch - '0']);
        }

        return result.toString();
    }

    private static String makePuzzle(String solution, int emptyCount, int seed) {

        char[] puzzle = solution.toCharArray();

        Random random = new Random(seed * 100L);

        int removed = 0;

        while (removed < emptyCount) {
            int index = random.nextInt(81);

            if (puzzle[index] != '0') {
                puzzle[index] = '0';
                removed++;
            }
        }

        return new String(puzzle);
    }
}