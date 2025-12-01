package domain.Exceptions;

public class ValidationException extends DomainException{

    private final String field;

    public ValidationException(String field , String message){
        super(message);
        this.field = field;
    }

    public String field() {
        return field;
    }

}
