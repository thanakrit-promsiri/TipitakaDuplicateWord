package buddhasasana.sangiti.tipitaka.index.duplicate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class TipitakaDuplicateWord {

    //public static final String FILEREPO = "tipitaka";
    public static final String FILEREPO = "THAISIAM";
    public static final String PREPARE = "/prepare";
    public static final String OUTPUT = "/output";
    public static final String INPUT = "/input";
    public static final String IGNORETXT = "/ignoretxt";

    public static final String FILESPACE = IGNORETXT + "/txt2space.txt";
    public static final String FILEREMOVE = IGNORETXT + "/txt2remove.txt";
    public static final String FILEINSERTSPACE = IGNORETXT + "/txt2insertspace.txt";
    public static final String FSPACE = "1";
    public static final String FREMOVE = "2";
    public static final String FINSERTSPACE = "3";

    public Map<String, Integer> reduceTxt;

    public TipitakaDuplicateWord(Map<String, Integer> reduceTxt) {
        this.reduceTxt = reduceTxt;
    }

    public static void main(String[] args) {

        Map<String, Integer> reduceTxt = new TreeMap<>();
        TipitakaDuplicateWord tipitaka = new TipitakaDuplicateWord(reduceTxt);

        tipitaka.processPrepareFile();
        tipitaka.processMapReducePrepare();
        tipitaka.processMapReduceCSV();
    }

    public void processPrepareFile() {
        String[] directory = this.checkDirectory(FILEREPO);
        for (String file : directory) {
            String path = FILEREPO + INPUT + "/" + file;
            String txt = this.getFile(path);
            this.processPrepare(txt.toLowerCase(), file);
            System.out.println(path);
        }
    }

    public void processMapReduceCSV() {
        StringBuilder csv = new StringBuilder();
        csv.append("word").append(",").append("TextCount").append(",").append("TextLength").append("\n");
        for (Map.Entry<String, Integer> entry : this.reduceTxt.entrySet()) {
            String key = entry.getKey().trim();
            Integer value = entry.getValue();
            if (!"".equals(key) && key != null) {
                csv.append(key).append(",").append(value).append(",").append(key.length()).append("\n");
            }
        }
        this.writeUsingFiles(csv.toString(), FILEREPO + OUTPUT + "/summary.csv");
    }

    public void processMapReducePrepare() {
        File file2 = new File(FILEREPO + PREPARE);
        System.out.println(FILEREPO + PREPARE);
        System.out.println("___________");
        this.printFile(file2.list());
        for (String file : file2.list()) {
            String prepare = FILEREPO + PREPARE + "/" + file;
            System.out.println(prepare);
            String txt = this.getFile(prepare);
            this.processMapReduce(txt);
        }
        System.out.println("___________");
    }

    public void processMapReduce(String txt) {
        String[] wordArr = txt.split(" ");
        for (String word : wordArr) {
            word = word.toLowerCase();
            if (this.reduceTxt.get(word) == null) {
                this.reduceTxt.put(word, 1);
            } else {
                int count = this.reduceTxt.get(word) + 1;
                this.reduceTxt.put(word, count);
            }

        }
    }

    public String processPrepare(String txt, String file) {

        txt = this.textRemove(txt);
        txt = this.textSpace(txt);
        txt = this.textInsertSpace(txt);
        this.writeUsingFiles(txt, FILEREPO + PREPARE + "/_" + file);
        return txt;
    }

    public String textRemove(String txt) {
        String path = FILEREPO + FILEREMOVE;

        return textReplaceByConfigFile(txt, path, FREMOVE);
    }

    public String textSpace(String txt) {
        String path = FILEREPO + FILESPACE;
        txt = textReplaceByConfigFile(txt, path, FSPACE);
        txt = txt.replaceAll("\n", " \n ");
        return txt;
    }

    public String textInsertSpace(String txt) {
        String path = FILEREPO + FILEINSERTSPACE;
        txt = textReplaceByConfigFile(txt, path, FINSERTSPACE);
        return txt;
    }

    public String textReplaceByConfigFile(String txt, String path, String flagReplace) {

        String replacement = "";
        if (FSPACE.equalsIgnoreCase(flagReplace)) {
            replacement = " ";
        }

        String texOrigin = getFile(path);
        List<String> txtConfList = textSplitLine(texOrigin);
        for (String txtConf : txtConfList) {
            if (!txtConf.equalsIgnoreCase("")) {
                if (FINSERTSPACE.equalsIgnoreCase(flagReplace)) {
                    txt = txt.replaceAll(txtConf, " " + txtConf + " ");
                    System.err.println(txtConf);
                } else {
                    txt = txt.replaceAll(txtConf, replacement);
                }
            }
        }
        return txt;
    }

    public List<String> textSplitLine(String str) {
        String[] strLineArr = str.split("\n");
        return Arrays.asList(strLineArr);
    }

    public String[] checkDirectory(String str) {
        this.setDirectory(str);
        this.setFilePrepare(str, PREPARE);
        this.setFilePrepare(str, OUTPUT);
        this.setFileIgnoreChar(str);
        String[] file = this.setDirectory(str + "/input");
        return file;
    }

    public String[] setDirectory(String str) {
        File file = new File(str);
        if (!file.exists()) {
            file.mkdir();
        }
        return file.list();
    }

    public void setFilePrepare(String str, String sub) {

        File filepre = new File(str + sub);
        if (!filepre.exists()) {
            filepre.mkdir();
        } else {
            clearFilePrepare(filepre);
        }
    }

    public void setFileIgnoreChar(String str) {

        File filepre = new File(str + IGNORETXT);
        if (!filepre.exists()) {
            filepre.mkdir();
            writeIgnoeFileSpacetxt(str + FILESPACE);
            writeIgnoeFileRemovetxt(str + FILEREMOVE);
            writeIgnoeFileRemovetxt(str + FILEINSERTSPACE);
        } else {

        }
    }

    public void clearFilePrepare(File filepre) {
        String[] entries = filepre.list();
        for (String s : entries) {
            File currentFile = new File(filepre.getPath(), s);
            currentFile.delete();
        }
    }

    public void printFile(String[] list) {
        for (String file : list) {
            System.out.println(file);
        }
    }

    protected String getFile(String fileName) {
        StringBuilder strBuilder = new StringBuilder();
        try {
            BufferedReader in = reader(fileName);
            String str;
            while ((str = in.readLine()) != null) {
                strBuilder.append(str).append("\n");
            }
        } catch (Exception e) {
        }
        return strBuilder.toString();
    }

    private BufferedReader reader(String fileName) throws Exception {

        FileInputStream stream = new FileInputStream(fileName);
        InputStreamReader input = new InputStreamReader(stream, "UTF-8");
        BufferedReader in = new BufferedReader(input);
        return in;
    }

    private void writeIgnoeFileSpacetxt(String path) {
        String data = "";
        this.writeUsingFiles(data, path);

    }

    private void writeIgnoeFileRemovetxt(String path) {
        String data = "";
        this.writeUsingFiles(data, path);

    }

    public void writeUsingFiles(String data, String path) {
        try {
            Files.write(Paths.get(path), data.getBytes());
        } catch (IOException e) {
        }
    }

}
