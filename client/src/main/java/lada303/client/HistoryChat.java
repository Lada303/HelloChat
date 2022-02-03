package lada303.client;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class HistoryChat {
    private static final File PATH_HISTORICAL = new File("client/history/");
    private static final int NUMBER_HISTORY_LINES_LOADED = 100;
    private final File historyFile;

    protected HistoryChat(String login) {
        this.historyFile = new File( PATH_HISTORICAL + "/history_" + login + ".txt");
    }

    public File getHistoryFile() {
        return historyFile;
    }

    protected void isExistsOrCreateHistoryFile() {
        if (!historyFile.exists()) {
            PATH_HISTORICAL.mkdirs();
            try (FileWriter fw = new FileWriter(historyFile)) {
                fw.write("");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected void writeInHistoryFile(List<String> listOfChatStrings) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(historyFile, true))) {
            for (String listOfChatString : listOfChatStrings) {
                bw.write(listOfChatString);
            }
            bw.write("--- end session ---\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected List<String> readFromHistoryFile() {
        List<String> listStr = new ArrayList<>();
        try {
             listStr = Files.readAllLines(historyFile.toPath());
        } catch (IOException e) {
            //e.printStackTrace();
            System.out.println("file read error" + e.getMessage());
        }
        int start = 0;
        if (listStr.size() >= NUMBER_HISTORY_LINES_LOADED) {
            start = listStr.size() - NUMBER_HISTORY_LINES_LOADED;
        }
        return listStr.subList(start, listStr.size());
    }

}
