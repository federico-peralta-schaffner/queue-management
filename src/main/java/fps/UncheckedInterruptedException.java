package fps;

public class UncheckedInterruptedException extends RuntimeException {

    public UncheckedInterruptedException(InterruptedException cause) {
        super(cause);
    }
}
