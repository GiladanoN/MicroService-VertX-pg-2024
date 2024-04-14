package com.giladanon.extraTestModule;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

import com.giladanon.sharedModule.Common.Exceptions.ConflictOnAdd;
import com.giladanon.sharedModule.Common.Exceptions.ValidationError;
import com.giladanon.sharedModule.Common.POJO.Order;
import com.giladanon.sharedModule.Common.POJO.OrderList;
import com.giladanon.sharedModule.Common.POJO.OrderToAdd;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.FileSystem;
import io.vertx.core.impl.Arguments;

public class OrderAdder {

  private OrderToAdd data;
  FileSystem fileSystem;

  private Order order; // = data.getOrderToAdd();
  private String username; // = data.getUser();
  private FileHandler fileHandler;

  private OrderList updatedList;
  
  public OrderAdder(OrderToAdd data, FileSystem fs) {
    this.data = data;
    this.fileSystem = fs;
  }

  public void validate() throws ValidationError {  // gaurd-clause style (cascading pre-requisites)

    if (data == null)
      throw new ValidationError("data object provided is null (missing data).");

    this.order = data.getOrderToAdd();
    this.username = data.getUser();

    if (order == null || username == null)
      throw new ValidationError("some required data-field provided is null (missing).");

    final boolean doSubValidation = false; // remove once implemented
    if (doSubValidation) {
      // !toAdd.valid || userStr.isEmpty()
      // TODO- add fields validation on order object
      throw new ValidationError(
        "some required sub-field(s) failed upon relevnt validation.");
    }

    if (fileSystem == null)
      throw new ValidationError(
        "fileSystem object is required, provided is null (cannot operate files).");

    // System.out.println("everything checks out, state is valid!"); // DEBUG
  }

  public Future<OrderList> addOrderProcedure() throws IOException {

    System.out.println("Starting the addOrderProcedure() .");

    Future<OrderList> procFuture =
    this.openOrCreateFile()
      .map( (AsyncFile file) -> fileHandler.readFileContents(file).onComplete(r->{}) )
      .compose( (Future<Buffer> fb) -> fb )
      .map( (Buffer contents) ->  fileHandler.parseOrdersJsonArray(contents).result() )
      .compose( (OrderList list) -> confirmOrderNotExistAlready(list, order) ) // throws ConflictOnAdd
      .onComplete( (AsyncResult<OrderList> result) -> {
        // Future<OrderList> future = result.succeeded() ?
        //   Future.succeededFuture(result.result()) : Future.failedFuture(result.cause());
        if (result.failed()) {
          System.out.println("should terminate here -- cause: " + result.cause().getMessage());
          return;
        }
        Future.succeededFuture(result.result())
        .map( (OrderList list) -> this.addOrderToList(list, this.order) ) // produces the updated list
        .andThen( (AsyncResult<OrderList> res) -> {
          fileHandler.writeContentsToFile( res.result() );  // dump list back to file
        })
        .andThen( ignore -> {
          Path path = this.getFullFilePath(); //.toRealPath();
          String pathStr = (path != null ? path.toString() : "UNCLEAR");
          System.out.println("writing to file has completed! file: " + pathStr);    
        }) ;
      });


    System.out.println("Successfuly completed addOrderProcedure() !");
    return procFuture;

  }

  public Future<AsyncFile> openOrCreateFile() throws IOException {

    if (username == null)  // shouldn't happen if validate ran successfully
      username = data.getUser();
    
    if (fileHandler == null)  // lazy init
      this.fileHandler = new FileHandler(this, username);
    
    Path path = fileHandler.ensureFilePath();
    return fileHandler.openDataFile(fileHandler.fullFilePath);
  }

  public OrderList addOrderToList(OrderList list, Order toAdd) {
    if (list == null) {
      String err = "An OrderList object is not provided - no container to update...";
      throw new NullPointerException(err);
    }
    if (this.order == null)
      System.out.println("An order object is not provided - nothing to add...");
    else {
      list.getOrdersList().add(order);  // add item to the list
    }

    this.updatedList = list;
    return list;
  }

  public Future<OrderList> confirmOrderNotExistAlready(OrderList list, Order toAdd) {

    if (list == null) {
      System.out.println("An OrderList object is not provided - assuming order is indeed new...");
      return Future.succeededFuture(list);
    }
    if (this.order == null) {
      System.out.println("An order object is not provided - cannot perform check...");
      Arguments.require(false, "Order object is not provided"); // throw Excpetion
    }

    final UUID toAddID = toAdd.getOrderID();
    final String toAddName = toAdd.getOrderName();  // test only
    
    // check if any existing item in the list, matches the OrderToAdd.ID provided (= the request)
    // find items which match OrderID field, and provide one of them if any were found.
    Optional<Order> existingItemWithMachingID = 
    list.getOrdersList().stream()
      // .filter( (Order o) -> o.getOrderID().equals(toAddID) )
      .filter( (Order o) -> o.getOrderName().equals(toAddName) )  // test only
      .findAny();
    
    if (existingItemWithMachingID.isPresent()) {
      System.out.println("found a confliting item - " + existingItemWithMachingID.get());
      return Future.<OrderList>failedFuture(
        new ConflictOnAdd("Another item with the same OrderID already exists."));
    }

    // no conflicts found in current list, can continue adding.
    System.out.println("no conflicts found in current list, can continue adding.");
    return Future.succeededFuture(list);
  }


  public void dumpListBackToFile() {
    fileHandler.writeContentsToFile(this.updatedList);
  }

  public Path getFullFilePath() {
    if (fileHandler == null) return null;
    return fileHandler.getFullFilePath();
  }

}
