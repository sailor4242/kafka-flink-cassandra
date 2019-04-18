package exceptions {

  import java.util.UUID

  abstract class StacklessException(message: String) extends Exception(message, null, false, false)

  final case class UserNotFound(uid: UUID) extends StacklessException(s"User $uid not found")

}
