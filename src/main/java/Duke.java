import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.io.BufferedWriter;
import java.io.FileWriter;

public class Duke {
  private Ui ui;
  private Parser parser;

  public static void main(String[] args) throws IOException {
    new Duke().run();
  }

  public Duke() {
    ui = new Ui();
    parser = new Parser();
  }

  public void run() throws IOException {
    ui = new Ui();
    handleLoad();
    ui.greeting();
    handleList();
    ui.initPrompt();

    String longCommand = ui.getCommand();
    String[] keywords = parser.parseCommand(longCommand);

    while (!keywords[0].equals("bye")) {
      ui.printSmallLine();
      try {
        switch (keywords[0]) {
          case "list":
            handleList();
            break;

          case "done":
            handleDone(keywords[1]);
            break;

          case "delete":
            handleDelete(keywords[1]);
            break;
          case "todo":
            if (keywords.length == 1) throw new EmptyDescriptionException("Todo");
            handleTodo(keywords[1]);
            break;

          case "event":
            if (keywords.length == 1) throw new EmptyDescriptionException("Event");
            handleEvent(keywords[1]);
            break;

          case "deadline":
            if (keywords.length == 1) throw new EmptyDescriptionException("Deadline");
            handleDeadline(keywords[1]);
            break;

          default:
            throw new UnknownCommandException(keywords[0]);
        }
      } catch (EmptyDescriptionException
          | MissingTimeException
          | UnknownCommandException
          | InvalidIndexException e) {
        System.out.println("    " + e);
      } catch (DateTimeParseException e) {
        System.out.println("    " + "Please enter date in the format yyyy-MM-dd HHmm");
      } catch (Exception e) {
        System.out.printf("    I don't know this error homie, take a look:\n    %s\n", e);
      } finally {
        ui.printSmallLine();
        longCommand = ui.getCommand();
        keywords = parser.parseCommand(longCommand);
      }
    }
    saveBaby();
    ui.bye();
  }

  public static void saveBaby() throws IOException {
    BufferedWriter taskWriter = new BufferedWriter(new FileWriter(".//saved-tasks.txt"));
    StringBuilder tasks = new StringBuilder();
    for (Task task : Task.tasks) {
      tasks.append(task.toSaveString()).append("\n");
    }
    taskWriter.write(tasks.toString());
    taskWriter.close();
  }

  public static void handleList() {
    System.out.println("    Here are the tasks in your list:");
    for (int i = 1; i <= Task.tasks.size(); i++) {
      System.out.println("    " + i + ". " + Task.tasks.get(i - 1));
    }
  }

  public void handleLoad() throws IOException {
    BufferedReader taskLoader = new BufferedReader(new FileReader(".//saved-tasks.txt"));
    String longCommand = taskLoader.readLine();
    LocalDateTime myDateObj;
    while (longCommand != null) {
      String[] keywords = longCommand.split(" \\|\\| ");
      Task cur = null;
      switch (keywords[1]) {
        case "todo":
          cur = new Todo(keywords[2]);
          break;
        case "deadline":
          cur = new Deadline(keywords[2], parser.stringToTime(keywords[3]));
          break;
        case "event":
          cur = new Event(keywords[2], parser.stringToTime(keywords[3]));
          break;
        default:
          System.out.println("error");
          break;
      }
      if (keywords[0].equals("1")) {
        assert cur != null;
        cur.done();
      }
      longCommand = taskLoader.readLine();
    }
    taskLoader.close();
  }

  public static void handleDone(String keyword) throws InvalidIndexException {
    int index;
    try {
      index = Integer.parseInt(keyword) - 1;
      Task.tasks.get(index).done();
    } catch (Exception e) {
      throw new InvalidIndexException(keyword);
    }
    System.out.println("    Nice! I've marked this task as done:");
    System.out.println("    " + Task.tasks.get(index));
  }

  public static void handleDelete(String keyword) throws InvalidIndexException {
    int index;
    Task delete;
    try {
      index = Integer.parseInt(keyword) - 1;
      delete = Task.tasks.get(index);
      Task.tasks.remove(delete);
    } catch (Exception e) {
      throw new InvalidIndexException(keyword);
    }
    System.out.println("    Nice! I've deleted this task:");
    System.out.println("    " + delete);
    System.out.println("    Now you have " + Task.tasks.size() + " tasks in the list.");
  }

  public static void handleEvent(String desc) throws MissingTimeException {
    String[] strArr = desc.split(" /at ", 2);
    if (strArr.length == 1) throw new MissingTimeException("Event");
    String todo = strArr[0];
    String time = strArr[1];
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HHmm");
    LocalDateTime myDateObj = LocalDateTime.parse(time, formatter);
    Event task = new Event(todo, myDateObj);
    System.out.println("    Got it. I've added this task:");
    System.out.printf("    %s\n", task);
    System.out.printf("    Now you have %d tasks in the list.\n", Task.tasks.size());
  }

  public static void handleTodo(String desc) {
    Todo task = new Todo(desc);
    System.out.println("    Got it. I've added this task:");
    System.out.printf("    %s\n", task);
    System.out.printf("    Now you have %d tasks in the list.\n", Task.tasks.size());
  }

  public static void handleDeadline(String desc) throws MissingTimeException {
    String[] strArr = desc.split(" /by ", 2);
    if (strArr.length == 1) throw new MissingTimeException("Deadline");
    String todo = strArr[0];
    String time = strArr[1];
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HHmm");
    LocalDateTime myDateObj = LocalDateTime.parse(time, formatter);
    Deadline task = new Deadline(todo, myDateObj);
    System.out.println("    Got it. I've added this task:");
    System.out.printf("    %s\n", task);
    System.out.printf("    Now you have %d tasks in the list.\n", Task.tasks.size());
  }
}
