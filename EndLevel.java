class EndLevel {
    static void activate(){
        Cutscene.draw(CS2.bin(), Main.screen, 0, 0, 0);

        Main.screen.setTextColor(9);

        Main.screen.setTextPosition(158 - HUD.digits(Main.score) * 6 / 2, 134);
        Main.screen.print(Main.score);
        Main.totalScore += Main.score;

        Main.screen.setTextPosition(158 - HUD.digits(Main.score) * 6 / 2, 102);
        Main.screen.print(Main.killCount);
        
        Main.screen.setTextPosition(158 - HUD.digits(Main.score) * 6 / 2, 70);
        Main.screen.print(Main.rescueCount);
        Cutscene.flushAndWait();
    }
}