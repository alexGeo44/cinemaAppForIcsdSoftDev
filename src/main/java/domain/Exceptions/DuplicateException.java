package domain.Exceptions;

public class DuplicateException extends DomainException{

    private final String resource;

    public DuplicateException(String resource , String message){

        super(message);
        this.resource = resource;

    }

    public String recource(){
        return resource;
    }
}
