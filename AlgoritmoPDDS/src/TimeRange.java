import java.time.LocalDateTime;

public class TimeRange {
    public LocalDateTime start;
    public LocalDateTime end;

    public LocalDateTime getStart() {
        return start;
    }

    public void setStart(LocalDateTime start) {
        this.start = start;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    public void setEnd(LocalDateTime end) {
        this.end = end;
    }

    public TimeRange(LocalDateTime start, LocalDateTime end) {
        this.start = start;
        this.end = end;
    }

    public boolean contains(LocalDateTime dateTime) {
        return (dateTime.isEqual(start) || dateTime.isAfter(start)) &&
               (dateTime.isBefore(end) || dateTime.isEqual(end));
    }
}
