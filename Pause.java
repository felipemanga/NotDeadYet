import femto.input.Button;

public class Pause {
    public static void activate(){
        Main.screen.setTextPosition(110-20, 88);
        Main.screen.println((pointer)"Paused");
        Main.screen.flush();
        Button.waitForPress();
    }
}