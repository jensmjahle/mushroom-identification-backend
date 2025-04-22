package ntnu.idi.mushroomidentificationbackend.model.enums;

public enum UserRequestStatus {
  NEW, //New request, just created and unprocessed
  PENDING, //Pending request, waiting for user to do something
  IN_PROGRESS, //In progress, currently being processed by an admin
  COMPLETED //Completed, the request has been completed
}
