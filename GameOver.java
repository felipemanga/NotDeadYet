public class GameOver {
    static void activate(){
        Cutscene.draw(CS3.bin(), Main.screen, 0, 0, 0);
        Main.totalScore += Main.score;
        Main.screen.setTextColor(9);
        Main.screen.setTextPosition(110 - HUD.digits(Main.totalScore) * 6 / 2, 120);
        Main.screen.print(Main.totalScore);
        Main.totalScore = 0;
        Cutscene.flushAndWait();
    }
}