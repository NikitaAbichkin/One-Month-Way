import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class Cat implements Animal{
    public String sound() { return "meow"; }
}

class Dog  implements Animal {
    public String sound() { return "woof"; }
}

interface  Animal{
    public String sound();
}
class Bird implements Animal{
    public String sound() { return "wie"; }

}



public class Main {
    static void main() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Animal cat = new Cat();
        Animal dog = new Dog();
        Animal bird = new Bird();

        Method catMethod = cat.getClass().getMethod("sound");
        Method dogMethod = dog.getClass().getMethod("sound");
        Method birdMethod = bird.getClass().getMethod("sound");

        dog.sound();


    }
}
