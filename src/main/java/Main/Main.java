package Main;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    //region ПОЛЯ И СОСТОЯНИЕ

    private static final Map<String, String> translate = new LinkedHashMap<>();
    private static final StringBuilder translated = new StringBuilder();
    private static List<String> contents = new ArrayList<>();

    private static ProcessBuilder runpr;

    private static Scanner scanner = new Scanner(System.in);

    private static String workingString;
    private static String placeholder = "###BU###";
    private static String path;
    private static String filename = "Comp";
    private static String[] new_args;

    private static boolean only_translate = false; // -ot
    private static boolean only_translate_and_compile = false; // -otc
    private static boolean tsundere = false; // -ts
    private static boolean save_java_code = false; // -s
    private static boolean invert_translate = false; // -i

    private static final String tempDir = new File(System.getProperty("java.io.tmpdir"), "javanya-" + ProcessHandle.current().pid()).getPath();

    private static final Logger logger = Logger.getLogger(Main.class.getName());

    private static void set_static_null(){
        only_translate = false;
        tsundere = false;
        save_java_code = false;
        only_translate_and_compile = false;
        invert_translate = false;
        filename = "Comp";
        logger.setLevel(Level.INFO);
        path = null;
    }

    private static void main_kernel(){

    }

    //endregion

    //region ТОЧКА ВХОДА

    public static void main(String[] args) throws Exception {
        System.setProperty("java.util.logging.SimpleFormatter.format", "%5$s%6$s%n");
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        Logger rootLogger = LogManager.getLogManager().getLogger("");

        rootLogger.setUseParentHandlers(false);
        for (Handler h : rootLogger.getHandlers()) {
            rootLogger.removeHandler(h);
        }
        StreamHandler handler = new StreamHandler(System.out, new SimpleFormatter()) {
            @Override
            public synchronized void publish(LogRecord record) {
                super.publish(record);
                flush();
            }
        };
        handler.setLevel(Level.FINE);
        rootLogger.addHandler(handler);

        translates();

        String[] currentArgs = args;

        while (true) {
            setPath(currentArgs);

            byte exit_code_controlPath = controlPath();
            if (exit_code_controlPath == 0) {
                if (invert_translate) {
                    processFileReverse(path);
                } else {
                    processFile(path);
                }
            }
            if (!invert_translate) {
                if (!only_translate) {
                    Compile();
                }
                if (!only_translate && !only_translate_and_compile) {
                    run_class_file();
                }
                if (!save_java_code) {
                    delete_Java_Class();
                }
            }

            set_static_null();

            if (new_args == null) {
                break;
            }
            boolean is_placeholder_array = Arrays.stream(new_args).allMatch(s -> s == null || s.isEmpty());
            if (is_placeholder_array) {
                break;
            }

            print_fi("Переход к следующему набору аргументов: " + Arrays.toString(new_args) + " Ня.", "Опять по новой да? Ну ладно раз просишь, вот тебе: " + Arrays.toString(new_args) + " Ня.");
            currentArgs = new_args;
            new_args = null;
        }
    }

    //endregion

    //region РАЗБОР АРГУМЕНТОВ И ФЛАГОВ

    private static void setPath(String[] args){
        if (args == null || args.length == 0 || args[0] == null) {

            print_in("\u001B[31m Кошко-девочка компилятор просит вас ввести директорию файла который надо расшифровать Ня.: \u001B[0m", "\u001B[31m Кошко-девочка компилятор приказывает тебе ввести путь к файлу потому что ей скучно Ня.: \u001B[0m");
            path = scanner.nextLine().trim();
            if (path.equals("-e")){
                print_fi("Выход по флагу -e Ня.", "Значит даже поговорить не успели а ты уже убегаешь Ня.");
                System.exit(-2);
            }

        } else{

            flags_control(args);
            if (path == null) {
                print_in("\u001B[31m Кошко-девочка компилятор просит вас ввести директорию файла который надо расшифровать Ня.: \u001B[0m", "\u001B[31m Кошко-девочка компилятор приказывает тебе ввести путь к файлу потому что ей скучно пожалуйста Ня.: \u001B[0m");
                path = scanner.nextLine().trim();
                if (path.equals("-e")) {
                    print_fi("Выход по флагу -e Ня.", "Опять убегаешь ну и ладно мне тоже дела не было Ня.");
                    System.exit(-1);
                }
            }
            print_fi("Итоговый путь к файлу: " + path + " Ня.", "Так вот куда ты меня тащишь ладно запомнила Ня.");
        }
    }

    private static void flags_control(String[] args){
        if (args.length > 0){
            byte i = 0;
            while(args.length > i) {
                switch (args[i]) {
                    case "-s":
                        save_java_code = true;
                        print_fi("Флаг -s: сохранение .java и .class включено Ня.", "Раз тебе так хочется хранить эти файлы ну храни мне не жалко Ня.");
                    break;
                    case "-o":
                        try {
                            filename = args[i + 1];
                            print_fi("Флаг -o: имя выходного файла " + filename + " Ня.", "Ну раз тебе так хочется назвать файл так уж и быть запомню Ня.");
                    } catch (NullPointerException e){
                        print_in("\u001B[31m Кошко-девочка компилятор не нашла имени файла после флага Ня. \u001B[0m", "\u001B[31m Ты забыл написать имя файла после -o ну и ладно назову сама Ня. \u001B[0m");
                        filename = "Comp";
                    }
                    break;
                    case"-ts":
                        tsundere = true;
                        print_fi("Флаг -ts: цундере-режим включён Ня.", "Ну раз ты так хочешь я тебе покажу какая я на самом деле Ня.");
                    break;
                    case"-v":
                        logger.setLevel(Level.FINE);
                        print_fi("Флаг -v: подробный вывод включён Ня.", "Раз включил подробности так и быть буду болтать больше обычного Ня.");
                    break;
                    case"-i":
                        invert_translate = true;
                        print_fi("Флаг -i: обратный перевод Java -> ДжаваНя включён Ня.", "Значит теперь наоборот да? Ну ладно раз тебе так надо Ня.");
                    break;
                    case"-ot":
                        only_translate = true;
                    break;
                    case"-help":
                        logger.info("-v \t-> debug function Nya. \n" +
                                "-o \"a\" \t-> output filename is \"a\" Nya. \n" +
                                "-s \t-> save .java and .class file Nya. \n" +
                                "-ts \t-> for tsundere Nya. \n" +
                                "-ot \t-> only translate Nya. \n" +
                                "-otc \t-> only translate and compile Nya. \n" +
                                "-i \t-> invert translation, Java to JavaNya Nya. \n" +
                                "-e \t-> exit Nya. \n" +
                                "-n \t-> all over again \n");
                    break;
                    case"-e":
                        System.exit(1);
                    break;
                    case"-n":
                        new_args = Arrays.copyOfRange(args, i + 1, args.length);
                    return;
                    default:
                        path = args[i];
                    break;
                }
                i++;
            }
        } else {
            print_fi("Флагов не обнаружено Ня.", "Флагов не нашла ну я если бы и нашла то я бы не стала выполнять твои хотелки Ня.");
        }
    }

    private static byte controlPath(){
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));

        try {
            Path Control_Path_Util = Paths.get(path);
            boolean isOk = Files.exists(Control_Path_Util) && Files.isRegularFile(Control_Path_Util);
            if (!isOk) {
                print_in("\u001B[31m Кошко-девочка компилятор не нашла такого файла Ня. \u001B[0m", "\u001B[31m Ты реально думал что я буду выполнять компиляцию ? ну всё равно там не было файла ты даже это не смог сделать Ня. \u001B[0m");
                System.exit(2);
            }
        } catch (InvalidPathException | NullPointerException e) {
            print_in("\u001B[31m Кошко-девочка компилятор не нашла ничего по этой директории Ня. \u001B[0m", "\u001B[31m Ты такой неаккуратный ! ну исправляйся ! Ня. \u001B[0m");
            System.exit(3);
        }

        if (invert_translate) {
            if (!path.endsWith(".java")) {
                print_in("\u001B[31m Кошко-девочка компилятор в этом режиме понимает только настоящую Джаву Ня. \u001B[0m", "\u001B[31m Дай мне нормальный джава файл а не эти твои закорючки Ня. \u001B[0m");
                System.exit(4);
            } else {
                return 0;
            }
        } else {
            if (!path.endsWith(".jnya")) {
                print_in("\u001B[31m Кошко-девочка компилятор понимает только по ДжавеНЯ Ня. \u001B[0m", "\u001B[31m Даже не думай что я буду смотреть твои записи на этом непонятном языке ! ... но можно попробовать ещё раз Ня. \u001B[0m");
                System.exit(4);
            } else {
                return 0;
            }
        }
        print_in("\u001B[31m Кошко-девочка компилятор не смогла пройти проверку с этим файлом Ня. \u001B[0m", "\u001B[31m Что ты указал ты ничего нормально сделать не можешь укажи ещё раз Ня.  \u001B[0m");
        System.exit(5);
        return 1;
    }

    //endregion

    //region ПЕРЕВОД

    private static void processFile(String path) throws IOException {
        translated.setLength(0);

        FileReader fileReader = new FileReader(path, StandardCharsets.UTF_8);

        BufferedReader BuffRead = new BufferedReader(fileReader);
        String line;
        String firstLine = BuffRead.readLine();

        if (firstLine != null && firstLine.equals("Тунтуру!")) {
            print_fi("Файл начинается с Тунтуру!, проверка пройдена Ня.", "Поздоровался значит, ну ладно замечу это себе Ня.");
        } else {
            print_in("\u001B[31m Кошко-девочка компилятор ушла в депрессию и запой Ня. \u001B[0m", "\u001B[31m Даже не поздоровался со мной... Но ты не подумай ты мне не нужен просто научно доказано что здороваться полезно для продуктивной работы Ня. \u001B[0m");
            System.exit(6);
            return;
        }

        while ((line = BuffRead.readLine()) != null) {

            workingString = line;
            contents.clear();
            print_fi("Строка перед переводом: " + line + " Ня.", "Так, что тут у тебя понаписано, " + line + " Ня.");

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
                    workingString = workingString.replaceFirst(placeholder, Matcher.quoteReplacement(originalQuote));
                }

                print_fi("Строка после перевода: " + workingString + " Ня.", "Вот что получилось, " + workingString + " Ня.");
                translated.append(workingString).append("\n");
            } catch (NullPointerException | IllegalArgumentException e){
                print_in("\u001B[31m Кошко-девочка компилятор запутался в твоих кавычках Ня. \u001B[0m" , "\u001B[31m Посмотрела я на твои кавычки и сразу поняла что делал ты только ты мог сделать такое мне пришлось всё проверять! Ня. \u001B[0m");
                System.exit(7);
            }
        }
    }

    private static Map<String, String> buildReverseTranslate() {
        Map<String, String> reverse = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : translate.entrySet()) {
            if (entry.getValue().trim().isEmpty()) {
                continue;
            }
            reverse.putIfAbsent(entry.getValue(), entry.getKey());
        }
        return reverse;
    }

    private static void processFileReverse(String path) throws IOException {
        translated.setLength(0);
        translated.append("Тунтуру!\n");

        Map<String, String> reverseTranslate = buildReverseTranslate();

        FileReader fileReader = new FileReader(path, StandardCharsets.UTF_8);
        BufferedReader BuffRead = new BufferedReader(fileReader);
        String line;

        while ((line = BuffRead.readLine()) != null) {

            workingString = line;
            contents.clear();
            print_fi("Строка перед обратным переводом: " + line + " Ня.", "Так, что тут у джавистов понаписано, " + line + " Ня.");

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

                for (String key : reverseTranslate.keySet()) {
                    workingString = workingString.replace(key, reverseTranslate.get(key));
                }

                for (String originalQuote : contents) {
                    workingString = workingString.replaceFirst(placeholder, Matcher.quoteReplacement(originalQuote));
                }

                print_fi("Строка после обратного перевода: " + workingString + " Ня.", "Вот что получилось наоборот, " + workingString + " Ня.");
                translated.append(workingString).append("\n");
            } catch (NullPointerException | IllegalArgumentException e){
                print_in("\u001B[31m Кошко-девочка компилятор запутался в твоих кавычках Ня. \u001B[0m" , "\u001B[31m Посмотрела я на твои кавычки и сразу поняла что делал ты только ты мог сделать такое мне пришлось всё проверять! Ня. \u001B[0m");
                System.exit(7);
            }
        }

        BuffRead.close();

        try (FileWriter writer = new FileWriter(filename + ".jnya")) {
            writer.write(translated.toString());
            print_fi("Файл сохранён: " + filename + ".jnya Ня.", "Перевела всё обратно и сохранила вот сюда, " + filename + ".jnya Ня.");
        } catch (IOException e) {
            print_in("\u001B[31m Кошко-девочка компилятор не смогла сохранить обратный перевод Ня. \u001B[0m", "\u001B[31m Ну вот опять не получилось сохранить, поищи проблему сам Ня. \u001B[0m");
            System.exit(8);
        }
    }

    //endregion

    //region КОМПИЛЯЦИЯ И ЗАПУСК

    private static void Compile() {
        String finalNya = translated.toString();

        try {
            new File(tempDir).mkdirs();
            try (FileWriter writer = new FileWriter(tempDir + File.separator + filename + ".java")) {
                writer.write(finalNya);
                print_fi("Файл сохранён: " + tempDir + File.separator + filename + ".java Ня.", "Записала твою мешанину сюда, " + tempDir + File.separator + filename + ".java Ня.");
            } catch (IOException e) {
                print_in("\u001B[31m Кошко-девочка компилятор не смогла записать файл в байт код Ня. \u001B[0m", "\u001B[31m Твои записи не переводятся не под 1 существующий язык ты вообще ничего без меня не можешь Ня. \u001B[0m");
                System.exit(8);
            }

            ProcessBuilder compilepr = new ProcessBuilder("javac", "-encoding", "UTF-8", tempDir + File.separator + filename + ".java");
            compilepr.inheritIO();
            Process compileprocess = compilepr.start();
            int compileExit = compileprocess.waitFor();
            print_fi("Код завершения javac: " + compileExit + " Ня.", "Джавак сказал " + compileExit + " и что мне с этим делать Ня.");
            if (compileExit != 0) {
                print_in("\u001B[31m Кошко-девочка компилятор не смогла скомпилировать Ня. \u001B[0m", "\u001B[31m Твои записи совершенно не понятны я может могу помогать такому как ты но только исключительно потому что ты такой жалкий сам Ня. \u001B[0m");
                System.exit(9);
            }
        } catch (IOException | InterruptedException e) {
            print_in("\u001B[31m Кошко-девочка компилятор уронила чернила Ня. \u001B[0m" , "\u001B[31m Я пока снимала скуку на твоем проекте нечаянно уранила чернила это из за того что у тебя тут такой бардак ! Ня. \u001B[0m");
            logger.fine(e.getMessage());
            System.exit(10);
        }
    }

    private static void run_class_file(){
        try {
            print_fi("Запуск: java -cp " + tempDir + " " + filename + " Ня.", "Запускаю только не думай что мне интересно чем всё кончится Ня.");
            runpr = new ProcessBuilder("java", "-Dfile.encoding=UTF-8", "-Dstdout.encoding=UTF-8", "-cp", tempDir, filename);
            runpr.inheritIO();
            Process runprocess = runpr.start();
            int exitCode = runprocess.waitFor();
            print_fi("Код завершения процесса: " + exitCode + " Ня.", "Вот тебе код " + exitCode + " и не спрашивай что это значит Ня.");
            if (exitCode == 0) {
                print_fi("Выполнение завершено успешно Ня.", "Получилось не то чтобы я радовалась просто скучно было сидеть без дела Ня.");
            } else {
                print_in("\u001B[31m Кошко-девочка компилятор тебя не поняла Ня. \u001B[0m" , "\u001B[31m Что тут вообще написано ничего не понятно ладно исправим что бы ты вообще без меня бы делал Ня. \u001B[0m");
                System.exit(12);
            }
        } catch (InterruptedException | IOException e) {
            print_in("\u001B[31m Кошко-девочка компилятор запуталась Ня. \u001B[0m", "\u001B[31m тут вообще ничего не понятно ты как всегда ничего не смог нормально сделать Ня. \u001B[0m");
            System.exit(13);
        }
    }

    private static void delete_Java_Class(){
        for (File f : new File(tempDir).listFiles())
            if (f != null) {
                f.delete();
            }
    }

    //endregion

    //region ЛОГИРОВАНИЕ И ПЕРСОНЫ

    public static void print_in(String originalMessage, String TsundereMessage) {
        if (tsundere) {
            logger.info(TsundereMessage);
        } else {
            logger.info(originalMessage);
        }
    }

    public static void print_fi(String originalMessage, String TsundereMessage) {
        if (tsundere) {
            logger.fine(TsundereMessage);
        } else {
            logger.fine(originalMessage);
        }
    }

    //endregion

    //region СЛОВАРЬ JavaNya

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

    //endregion
}
