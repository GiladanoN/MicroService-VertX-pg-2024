package com.giladanon.extraTestModule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import com.giladanon.sharedModule.Common.POJO.OrderList;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.impl.Arguments;
import io.vertx.core.json.Json;

class FileHandler {

  /**
   *
   */
  private final OrderAdder orderAdder;

  private final Path folderPath =
    Paths.get(".", "data", "orders");  // hardcoded
  
  private final OpenOptions options =
    new OpenOptions()
      .setCreate(true)  // create file if missing, without error if already exists
      .setTruncateExisting(false) // ensure contents are preserved (if present)
      // .setAppend(true)  // open writing in append-mode (assumes file already has contents)
      .setRead(true)
      .setWrite(true);  // allow read AND write ops.
  
  private String filename;
  Path fullFilePath;

  private Future<AsyncFile> openFile;

  public FileHandler(OrderAdder orderAdder, String filename) {
    this.orderAdder = orderAdder;
    this.filename = filename + ".json";
    this.fullFilePath =
      Paths.get(folderPath.toString(), this.filename);
  }

  public Path ensureFilePath() throws IOException {

    System.out.println("Running --- ensureFilePath()");
    // create directories to allow file creation / update
    try {
      return Files.createDirectories(folderPath);
    }
    catch (IOException e) {
      System.out.println(
        "ERORR :: Skipping file opening/writing, direcrtor(ies) could not be created.\n" +
        "relevant path: " + folderPath
      );
      e.printStackTrace();
      throw e;  // propagate the error once printed above.
    }

  }

  public Future<AsyncFile> openDataFile(Path path) {
    System.out.println("Running --- openDataFile()");
    this.openFile =
      this.orderAdder.fileSystem
      .open(path.toString(), options)
      .andThen(res -> {
        String msg = (res.failed()) ?
          "A problem occured when opening the file" : "Success in opening/creating the file";
        System.out.println(msg + ", path: " + path);
      }
    );
    return openFile;
  }

  public Future<Buffer> readFileContents(AsyncFile openFile) {

    System.out.println("Running --- readFileContents()");

    // long readLength = openFile.size().onFailure(
    //   f -> { System.out.println("Cannot determine file size"); f.printStackTrace(); }
    // ).result();
    long readLength = openFile.sizeBlocking();  // TODO
    System.out.println("got readLen = " + readLength);
    Buffer readBuffer = Buffer.buffer((int)readLength);
    System.out.println("> File size to read detected as: (in bytes) " + readLength);
    warnIfFileTooBig(readLength);

    // System.out.println("Exiting --- readFileContents() via 'readFile(path)'");
    // if (true)
    //   return fileSystem.readFile(fullFilePath.toString());
    
    Future<Buffer> futureBuffer =
      openFile.read(readBuffer, 0, 0, (int)readLength)
      // .onComplete(result ->
      // {
      //   if (result.succeeded()) {
      //     Buffer contentBuffer = result.result();
      //     System.out.println("Success in reading file contents, len=" + contentBuffer.length());
      //   }
      //   else {
      //     System.out.println("A problem occured when reading a file, path: " + filename);
      //   }
      //   // return result;
      // })
      ;

    System.out.println("> called 'read' function");

    futureBuffer.result();
    futureBuffer.onFailure(
      f -> { System.out.println("futureBuffer returned with error.."); f.printStackTrace(); }
    );

    System.out.println("Exiting --- readFileContents()");
    return futureBuffer;
  }

  public Future<OrderList> parseOrdersJsonArray(Buffer bufferArg) {
    System.out.println("Running --- parseOrdersJsonArray()");

    if (bufferArg == null) {
      System.out.println("Running --- bufferArg provided is null...");
      Arguments.require(false, "bufferArg provided is null");
      // bufferArg = readFileContents().result();
    }

    Promise<OrderList> promise = Promise.<OrderList>promise();
    Future<OrderList> parsedList = promise.future();

    Buffer readBuffer = bufferArg;
    
    if (readBuffer.length() == 0) {
      System.out.println("nothing found to parse in given file, path: " + filename);
      promise.complete(new OrderList());  // resolve with empty list (= empty file)
    }
    else {  // file has contents
      System.out.println("trying to parse the given file as OrderList.");
      try {
        OrderList list = readBuffer.toJsonObject().mapTo(OrderList.class);
        promise.complete(list);
        System.out.println("parsing completed successfylly. list: " + list);
      }
      catch (Exception e) {
        System.out.println("an error was thrown during parsing.");
        e.printStackTrace();
        promise.fail(e);
      }
    }

    return parsedList;
  }

  public void writeContentsToFile(OrderList list) {
    System.out.println("Running --- writeContentsToFile()");

    if (list == null) {
      System.out.println("Nothing provided to update the file with... replaceing with empty list");
      list = new OrderList();
      list.setOrdersList(Collections.emptyList());
    }
    Buffer buffer = Buffer.buffer("");
    try {
      String output = Json.encodePrettily(list);
      buffer.appendString(output);

      // ensure file gets overwritten to avoid corrupted json
      this.orderAdder.fileSystem.writeFile(fullFilePath.toString(), buffer);

      // AsyncFile asyncFile = this.openFile.result();
      // asyncFile.setWritePos(0).end(buffer);  // pottential "junk at end" issue, needs checking
    }
    catch (Exception e) {
      System.out.println("Something went wrong preparing or writing the data out...");
      e.printStackTrace();
      throw e;
    }
    
  }

  public Path getFullFilePath() {
    return fullFilePath;
  }

  private void warnIfFileTooBig(long fileLength) {
    if (fileLength > Integer.MAX_VALUE) {
      System.out.println(
        "WARNING :: possible issue with buffer size limitation, file size larger than " +
          + Integer.MAX_VALUE + " bytes (Integer.MAX_VALUE) -- value: " + fileLength);
    }
  }  

}