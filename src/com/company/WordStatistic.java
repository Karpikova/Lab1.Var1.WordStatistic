package com.company;
/*
 * ${Classname}
 * 
 * Version 1.0 
 * 
 * 07.04.2017
 * 
 * Karpikova
 */
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WordStatistic implements Runnable{

    private Map<String, Integer> local_result;
    private String fileName;
    private ResultStatistics resultStatistics;

    public WordStatistic(String fileName, ResultStatistics result) {
        this.fileName = fileName;
        this.resultStatistics = result;
    }

    @Override
    public void run() {
        local_result = new HashMap<>();
        String allText = "";
        File file_local = new File(fileName);
        URL url = null;
        boolean itIsLocalFile = file_local.exists();
        if (itIsLocalFile){
            allText = ReadLocalFile(file_local);
        } else {
            url = FileRemoteExsists(fileName);
            if (url != null) {
                allText = ReadRemoteFile(url);
            } else {
                System.out.println("File " + fileName + " is not found.");
                return;
            }
        }
        HandleText(allText);
        SetCommonVariable();
    }

    private String ReadRemoteFile(URL url) {
        String allText = "";
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(url.openStream()));) {
            String inputLine;
            while ((inputLine = bufferedReader.readLine()) != null) {
                allText = allText.concat(inputLine);
            }
        } catch (IOException e) {
            System.out.println("Sorry, IOExeption problem");
            e.printStackTrace();
        }
        finally {
            return allText;
        }
    }

    private URL FileRemoteExsists(String fileName) {
        URL url = null;
        try {
            url = new URL(fileName);
        } catch (MalformedURLException e) {
            System.out.println("Ooops, we have a MalformedURLException, sorry.");
            e.printStackTrace();
            return null;
        }

        HttpURLConnection httpURLConnection = null;
        try {
            httpURLConnection = (HttpURLConnection) url.openConnection();
            if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                return url;
            }
            else {
                return null;
            }
        } catch (IOException e) {
            System.out.println("Ooops, checking remote file exsisting, we got an IOException, sorry.");
            e.printStackTrace();
            return null;
        }
        finally {
            if (httpURLConnection != null) {
                try {
                    httpURLConnection.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String ReadLocalFile(File file_local) {
        String allText = "";
        try (BufferedReader reader = new BufferedReader (new FileReader(file_local));)
        {
            while (reader.ready()) {
                allText = allText.concat(" ").concat(reader.readLine());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Sorry, IOExeption problem");
            e.printStackTrace();
        }
        finally {
            return allText;
        }
    }

    private void HandleText(String allText) {
    /*Артем, как лучше сделать? Каждый раз передавать аргументом метода заранее созданный паттерн
    pWord или каждый раз создавать его заново? Я склоняюсь ко второму варианту, т.к. затраты по памяти
    ничтожно малы (мне так кажется), зато лишний параметр не передаем в метод - М.Роберт советует
    минимизировать количество передаваемых параметров
     */
        Pattern pWord = Pattern.compile("\\b.+?\\b");
        Matcher next_word = pWord.matcher(allText);
        while (next_word.find())
        {
            int count_words = 0;
            String word = next_word.group();

            Integer value = local_result.get(word);
            if (! ContainsOnlyRus(word)) continue;
            if (value == null)
            {
                Pattern thisWord = Pattern.compile("\\b"+word+"\\b");
                Matcher this_word = thisWord.matcher(allText);
                while(this_word.find()) count_words++;
                local_result.put(word, count_words);
            }
        }
    }

    private boolean ContainsOnlyRus(String word) {
        Pattern pWord = Pattern.compile("[а-яА-Я]*");
        Matcher mWord = pWord.matcher(word);
        return ((mWord.matches()) ? true : false);
    }

    private void SetCommonVariable() {
        String word;
        Integer count;
        for (Map.Entry<String, Integer> item : local_result.entrySet()) {
            word = item.getKey();
            count = item.getValue();

            synchronized (resultStatistics) {
                if (resultStatistics.result.containsKey(word)) {
                    resultStatistics.result.put(word, count + (int) resultStatistics.result.get(word));
                } else {
                    resultStatistics.result.put(word, count);
                }
            }
        }
    }

    public static void PrintStatistics(ResultStatistics resultStatistics){
        String word;
        Integer count;
        Date currentDate = new Date();
        System.out.println("Statistics, " + currentDate + ":");
        for (Map.Entry<String, Integer> item : resultStatistics.result.entrySet()) {
            word = item.getKey();
            count = item.getValue();
            System.out.println(count + " " + word);
        }
        System.out.println();
    }
}
