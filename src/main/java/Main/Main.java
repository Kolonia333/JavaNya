package Main;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

public class Main {

    private static final Map<String, String> translate = new LinkedHashMap<>();
    private static final StringBuilder translated = new StringBuilder();
    private static List<String> contents = new ArrayList<>();

    private static ProcessBuilder runpr;
    private static Scanner scanner = new Scanner(System.in);
    private static String workingString;
    private static String placeholder = "###BU###";


    public static void main(String[] args) throws Exception {
        System.setOut(new PrintStream(System.out, true, java.nio.charset.StandardCharsets.UTF_8));

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            for (File f : new File("src/main/java/temp").listFiles())
                f.delete();
        }));

        translates();
        System.out.print("\u001B[31m Введите директорию файла который надо расшифровать Ня.: \u001B[0m");
        String path = scanner.nextLine();

        System.setOut(new PrintStream(System.out, true, java.nio.charset.StandardCharsets.UTF_8));
        try {
            if (!path.endsWith(".jnya")) {
                System.out.println("\u001B[31m кошко-девочка компилятор понимает только по ДжавеНЯ Ня. \u001B[0m");
            } else {
                processFile(path);
            }
        } catch (IOException e) {
            System.out.print("\u001B[31m Кошко девочка компилятор не нашла такого файла Ня. \u001B[0m");
        }
    }

    private static void processFile(String path) throws IOException {
        translated.setLength(0);

        FileReader fileReader = new FileReader(path, java.nio.charset.StandardCharsets.UTF_8);

        BufferedReader BuffRead = new BufferedReader(fileReader);
        String line;
        String firstLine = BuffRead.readLine();

        if (firstLine != null && firstLine.equals("Тунтуру!")) {
        } else {
            System.out.println("\u001B[31m Кошко девочка компилятор ушел в депрессию и запой Ня. \u001B[0m");
            return;
        }

        while ((line = BuffRead.readLine()) != null) {

            workingString = line;
            contents.clear();

            try {
                int start = line.indexOf('"');
                while (start != -1) {
                    int end = line.indexOf('"', start + 1);
                    if (end != -1) {
                        contents.add(line.substring(start, end + 1));
                        start = line.indexOf('"', end + 1);
                    } else {
                        break;
                    }
                }

                for (String content : contents) {
                    workingString = workingString.replaceFirst(Pattern.quote(content), placeholder);
                }

                for (String key : translate.keySet()) {
                    workingString = workingString.replace(key, translate.get(key));
                }

                for (String originalQuote : contents) {
                    workingString = workingString.replaceFirst(placeholder, originalQuote);
                }

                translated.append(workingString).append("\n");
            } catch (NullPointerException | IllegalArgumentException e){
                System.out.println("\u001B[31m Кошко девочка компилятор запутался в твоих кавычках Ня. \u001B[0m");
            }
        }
        Compile();
    }
    private static void Compile(){
        String finalNya = translated.toString();

        try {
            new File("src/main/java/temp").mkdirs();
            try (FileWriter writer = new FileWriter("src/main/java/temp/Comp.java")) {
                writer.write(finalNya);
            } catch (IOException e) {
                System.out.println("\u001B[31m Кошко девочка компилятор не смогла записать файл в байт код Ня. \u001B[0m");
                return;
            }

            ProcessBuilder compilepr = new ProcessBuilder("javac", "-encoding", "UTF-8", "src/main/java/temp/Comp.java");
            compilepr.inheritIO();
            Process compileprocess = compilepr.start();
            int compileExit = compileprocess.waitFor();
            if (compileExit != 0) {
                System.out.println("\u001B[31m Кошко девочка компилятор не смогла скомпилировать Ня. \u001B[0m");
                return;
            }

            runpr = new ProcessBuilder("java", "-Dfile.encoding=UTF-8", "-Dstdout.encoding=UTF-8", "-cp", "src/main/java/temp", "Comp");
            runpr.inheritIO();
            Process runprocess = runpr.start();

            try {
                int exitCode = runprocess.waitFor();
                if (exitCode == 0) {

                    System.out.println("" + "\u001B[31m Кошко девочка компилятор любит тебя Ня. \u001B[0m");
                } else {
                    System.out.println("\u001B[31m Кошко девочка компилятор тебя не поняла Ня. \u001B[0m");
                }
            } catch (InterruptedException e) {
                System.err.println("\u001B[31m Кошко девочка компилятор запуталась Ня. \u001B[0m");
            } finally {
                for (File f : new File("src/main/java/temp").listFiles())
                    f.delete();
            }

        } catch (IOException | InterruptedException e) {
            System.err.println("\u001B[31m Кошко-девочка компилятор уронила чернила Ня. \u001B[0m" + e.getMessage());
        }
    }
    private static void translates() {
    translate.put("мяукнуть", "System.out.print");
    translate.put("научка", "import");
    translate.put("лаборатория", "public");
    translate.put("работник", "private");
    translate.put("Врата", "{");
    translate.put("Штейна", "}");
    translate.put( "Кристина", "args");
    translate.put("ассистентка", "//");
    translate.put("Ня.", ";");
    translate.put("мяукнем", "System.out.println");
    translate.put("Курисо", "String");
    translate.put("окабэ", "float");
    translate.put("маюри", "byte");
    translate.put("фейрис", "long");
    translate.put("итару", "double");
    translate.put("рука", "boolean");
    translate.put("йуга", "short");
    translate.put("моека", "int");
    translate.put("шип?", "==");
    translate.put("шип", "=");
    translate.put("гаджеты", "class");
    translate.put("Гаджет", "package");
    translate.put("джон", "while");
    translate.put("тайтер", "for");
    translate.put("Аттрактор", "System.exit(11037)");
    translate.put("петля", "return");
    translate.put("парадокс", "void");
    translate.put("Някод", "main");
    translate.put("альфа", "static");
    translate.put("итерация", "continue");
    translate.put("телефон", "switch");
    translate.put("номер", "case");
    translate.put("Д_Маил", "extends");
    translate.put("возврат", "continue");
    translate.put("поломка", "break");
    translate.put("Рлиния", "final");
    translate.put("чтение", "if");
    translate.put("развилка", "else");
    translate.put("пустота", "null");
    translate.put("Окабэ", "Float");
    translate.put("Маюри", "Byte");
    translate.put("Фейрис", "Long");
    translate.put("Итару", "Double");
    translate.put("Рука", "Boolean");
    translate.put("Йуга", "Short");
    translate.put("Моека", "Integer");
    translate.put("Инкубация", "new");
    translate.put("эго", "this");
    translate.put("связь", "interface");
    translate.put("Протокол", "implements");
    translate.put("попытка", "try");
    translate.put("предательство", "catch");
    translate.put("наблюдатель", "instanceof");
    translate.put("Эп", "(");
    translate.put("сай", ")");
    translate.put("Конгуру!", "char");
    translate.put("И", "&&");
    translate.put("ИЛИ", "||");
    translate.put("НЕ", "!");
    translate.put("СИЛЬНЕЕ?", ">=");
    translate.put("СЛАБЕЕ?", "<=");
    translate.put("СИЛЬНЕЕ", ">");
    translate.put("СЛАБЕЕ", "<");
    translate.put("Эп_Сай_Конгуру!", "throws");
    translate.put("плюс", "+");
    translate.put("минус", "-");
    translate.put("умножить", "*");
    translate.put("разделить", "/");
    translate.put("остаток", "%");
    translate.put("цепочка", "ArrayList");
    translate.put("добавить_в_линию", "add");
    translate.put("размер_линии", "size()");
    translate.put("гелевый_банан", "do");
    translate.put("путешествие", "Thread");
    translate.put("часы_остановились", "sleep");
    translate.put("Хасида", "HashMap");
    translate.put("Урусибара", "abstract");
    translate.put("экспериментальное", "protected");
    translate.put("забыть", "transient");
    translate.put("нестабильное", "volatile");
    translate.put("выбор", "default");
    translate.put("наследие", "super");
    translate.put("истинное", "true");
    translate.put("ложное", "false");
    translate.put("взять", "get");
    translate.put("установить", "set");
    translate.put("длина", "length");
    translate.put("содержит", "contains");
    translate.put("удалить", "remove");
    translate.put("объединить", "concat");
    translate.put("перевести_в_нижний_регистр", "toLowerCase");
    translate.put("перевести_в_верхний_регистр", "toUpperCase");
    translate.put("подстрока", "substring");
    translate.put("индекс_первого_вхождения", "indexOf");
    translate.put("индекс_последнего_вхождения", "lastIndexOf");
    translate.put("заменить", "replace");
    translate.put("раздвинуть", "split");
    translate.put("пробел", " ");
    translate.put("начать", "start");
    translate.put("записать", "write");
    translate.put("прочитать", "read");
    translate.put("Сина", "clear");
    translate.put("Кирю", "synchronized");
    translate.put("Акиха", "instanceof");
    translate.put("Аманэ", "wait");
    translate.put("Макисэ", "notify");
    translate.put("Мяу", "delete");
    }
}
