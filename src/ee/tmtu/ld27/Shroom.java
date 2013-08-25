package ee.tmtu.ld27;

import aurelienribon.tweenengine.*;

import ee.tmtu.ld27.entity.TextEntity;
import ee.tmtu.ld27.world.World;
import ee.tmtu.libludum.assets.AssetManager;
import ee.tmtu.libludum.core.ColorAccessor;
import ee.tmtu.libludum.core.Game;
import ee.tmtu.libludum.core.GameSettings;
import ee.tmtu.libludum.graphics.Font;
import ee.tmtu.libludum.graphics.SpriteBatch;
import ee.tmtu.libludum.graphics.Texture;
import ee.tmtu.libludum.sound.Audio;
import ee.tmtu.libludum.sound.Sound;
import ee.tmtu.libludum.ui.*;
import ee.tmtu.libludum.ui.event.KeyEvent;
import ee.tmtu.libludum.ui.event.KeyListener;
import ee.tmtu.libludum.ui.event.MouseEvent;
import ee.tmtu.libludum.ui.event.MouseListener;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.Color;
import org.lwjgl.util.ReadableColor;

import static org.lwjgl.opengl.GL11.*;

public class Shroom extends Game {

    public static final String TITLE = "Word\247io";
    public static final boolean[] keys = new boolean[256];
    public static final TweenManager TWEEN = new TweenManager();
    public boolean inGame;
    public Texture silhouette;
    public Texture blank;
    public SpriteBatch batch;
    public Sound purple_retort;
    public Sound win;
    public Sound fail;
    public Root root;
    public Font bigFont;
    public Font font;
    public World world;
    public Color overlay;

    public Shroom(GameSettings settings) {
        super(settings);
    }

    @Override
    public void init() {
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0.f, this.settings.width, this.settings.height, 0.f, 0.f, 1.f);
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        glEnable(GL_TEXTURE_2D);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDisable(GL_ALPHA_TEST);

        Tween.registerAccessor(Color.class, new ColorAccessor());

        this.blank = AssetManager.load("./assets/img/blank.png", Texture.class);
        this.silhouette = AssetManager.load("./assets/img/silhouette.png", Texture.class);
        this.batch = new SpriteBatch(1000);
        this.root = new Root(new Margin(0), new Padding(15));
        this.root.x = this.settings.width / 2 - 125;
        this.root.y = this.settings.height / 2 - 100;
        this.root.width = 250;
        this.bigFont = AssetManager.load("./assets/font/advocut30.fnt", Font.class);
        this.font = AssetManager.load("./assets/font/advocut19.fnt", Font.class);
        this.purple_retort = AssetManager.load("./assets/sound/bu-a-purple-retort.wav", Sound.class);
        this.win = AssetManager.load("./assets/sound/win.wav", Sound.class);
        this.fail = AssetManager.load("./assets/sound/fail.wav", Sound.class);
        this.world = new World();
        this.overlay = new Color(0, 0, 0, 255);
        Tween.to(overlay, ColorAccessor.A, 1.f).target(0.f).ease(TweenEquations.easeInOutQuad).start(Shroom.TWEEN);
        this.purple_retort.setLooping(true);
        //Audio.play(this.purple_retort);

        this.initMenu();
    }

    public void initHiScore() {
        this.root.clear();
        //this.world.entities.clear();
        this.inGame = false;

        for(int i = 5; i > 0; i--) {
            Button btn = new Button("Round " + (i), this.font);
            btn.padding = new Padding(10);
            btn.listener = new MouseListener() {
                @Override
                public void onMouseEvent(MouseEvent event) {
                    Tween.to(overlay, ColorAccessor.A, 1.f).target(1.f).start(Shroom.TWEEN);
                    Tween.to(overlay, ColorAccessor.A, 1.f).target(0.f).delay(1.f).start(Shroom.TWEEN);
                    Tween.call(new TweenCallback() {
                        @Override
                        public void onEvent(int i, BaseTween<?> baseTween) {
                            Shroom.this.initGame();
                        }
                    }).delay(1.f).start(Shroom.TWEEN);
                }
            };
            this.root.add(btn);
        }

        Button menu = new Button("Return", this.font);
        menu.padding = new Padding(10);
        menu.listener = new MouseListener() {
            @Override
            public void onMouseEvent(MouseEvent event) {
                Tween.to(overlay, ColorAccessor.A, 1.f).target(1.f).start(Shroom.TWEEN);
                Tween.to(overlay, ColorAccessor.A, 1.f).target(0.f).delay(1.f).start(Shroom.TWEEN);
                Tween.call(new TweenCallback() {
                    @Override
                    public void onEvent(int i, BaseTween<?> baseTween) {
                        Shroom.this.initMenu();
                    }
                }).delay(1.f).start(Shroom.TWEEN);
            }
        };
        this.root.add(menu);
        this.root.layout();
    }

    public void initGame() {
        this.root.clear();
        this.world.entities.clear();
        this.inGame = true;
        final Button button = new Button("Submit", this.font);
        button.padding = new Padding(10);
        final TextField tfield = new TextField(this.font);
        tfield.padding = new Padding(10);
        tfield.listener = new KeyListener() {
            @Override
            public boolean onKey(KeyEvent event) {
                if(event.state == KeyEvent.KeyState.DOWN) {
                    if(event.key == Keyboard.KEY_RETURN) {
                        if(Shroom.this.world.submit(tfield.str)) {
                            Audio.play(Shroom.this.win);
                            System.out.println("Success!");
                        } else {
                            Audio.play(Shroom.this.fail);
                            System.out.println("pone");
                        }
                        tfield.str = "";
                        button.state = Component.ComponentState.DOWN;
                        button.drawable = button.click;
                        return true;
                    }
                } else {
                    button.state = Component.ComponentState.IDLE;
                    button.drawable = button.idle;
                }
                return false;
            }
        };
        button.listener = new MouseListener() {
            @Override
            public void onMouseEvent(MouseEvent event) {
                if(Shroom.this.world.submit(tfield.str)) {
                    Audio.play(Shroom.this.win);
                    System.out.println("Success!");
                } else {
                    Audio.play(Shroom.this.fail);
                    System.out.println("pone");
                }
                tfield.str = "";
            }
        };
        this.root.add(tfield);
        this.root.requestFocus(tfield);
        this.root.add(button);
        this.root.layout();
        this.world.entities.add(new TextEntity(this.world, 200, 10, this.font, "mitch_"));
    }

    public void initMenu() {
        this.inGame = false;
        this.world.entities.clear();
        this.root.clear();
        Button play = new Button("Play Game", this.font);
        play.padding = new Padding(10);
        play.listener = new MouseListener() {
            @Override
            public void onMouseEvent(MouseEvent event) {
                Tween.to(overlay, ColorAccessor.A, 1.f).target(1.f).start(Shroom.TWEEN);
                Tween.to(overlay, ColorAccessor.A, 1.f).target(0.f).delay(1.f).start(Shroom.TWEEN);
                Tween.call(new TweenCallback() {
                    @Override
                    public void onEvent(int i, BaseTween<?> baseTween) {
                        Shroom.this.initGame();
                    }
                }).delay(1.f).start(Shroom.TWEEN);
            }
        };
        Button hiscore = new Button("Hi-score", this.font);
        hiscore.padding = new Padding(10);
        hiscore.listener = new MouseListener() {
            @Override
            public void onMouseEvent(MouseEvent event) {
                Tween.to(overlay, ColorAccessor.A, 1.f).target(1.f).start(Shroom.TWEEN);
                Tween.to(overlay, ColorAccessor.A, 1.f).target(0.f).delay(1.f).start(Shroom.TWEEN);
                Tween.call(new TweenCallback() {
                    @Override
                    public void onEvent(int i, BaseTween<?> baseTween) {
                        Shroom.this.initHiScore();
                    }
                }).delay(1.f).start(Shroom.TWEEN);
            }
        };
        Button quit = new Button("Quit", this.font);
        quit.padding = new Padding(10);
        quit.listener = new MouseListener() {
            @Override
            public void onMouseEvent(MouseEvent event) {
                Shroom.this.shutdown();
            }
        };
        this.root.add(play);
        this.root.add(hiscore);
        this.root.add(quit);
        this.root.layout();

        String[] themes = AssetManager.load("./assets/data/themes.txt", String[].class);
        int half = themes.length / 2;
        for(int i = 0; i < half; i++) {
            for(int i1 = 0; i1 < themes.length - half; i1++) {
                String str = themes[i+i1];
                this.world.entities.add(new TextEntity(this.world, i1 * (800.f / themes.length)*2, (i-4) * (1200.f / themes.length)*2, this.font, str));
            }
        }
    }

    double tenseconds = 10.f;

    @Override
    public void update() {
        Shroom.TWEEN.update((float) (1. / 20.));
        this.tenseconds -= 1. / 20.;

        for(int i = 0; i < 256; i++) {
            Shroom.keys[i] = Keyboard.isKeyDown(i);
        }
        KeyEvent ke = new KeyEvent();
        while(Keyboard.next()) {
            ke.key = Keyboard.getEventKey();
            ke.meta = Keyboard.isKeyDown(Keyboard.KEY_LMETA);
            ke.shift = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT);
            ke.ch = Keyboard.getEventCharacter();
            if(Keyboard.getEventKeyState()) {
                ke.state = KeyEvent.KeyState.DOWN;
            } else {
                ke.state = KeyEvent.KeyState.UP;
            }
            root.fire(ke);
        }
        MouseEvent me = new MouseEvent();
        while(Mouse.next()) {
            me.btn = Mouse.getEventButton();
            me.x = Mouse.getEventX();
            me.y = this.settings.height - Mouse.getEventY();
            me.dx = Mouse.getEventDX();
            me.dy = Mouse.getEventDY();
            me.state = MouseEvent.MouseState.NULL;
            if(me.dx != 0 || me.dy != 0) {
                me.state = MouseEvent.MouseState.MOVE;
            }
            if(me.btn > -1) {
                if(Mouse.getEventButtonState()) {
                    me.state = MouseEvent.MouseState.DOWN;
                } else {
                    me.state = MouseEvent.MouseState.UP;
                }
            }
            root.fire(me);
        }
        this.world.update();
        this.root.update();
    }

    @Override
    public void draw(double lerp) {
        glClear(GL_COLOR_BUFFER_BIT);
        glClearColor(129.f / 255.f, 109.f / 255.f, 80.f / 255.f, 1.f);

        this.batch.start();

        this.world.draw(this.batch, lerp);

        if(!this.inGame) {
            this.bigFont.draw(batch, this.settings.width / 2, this.settings.height / 2 - 135, Shroom.TITLE, Font.Orientation.CENTER);
        }

        this.root.draw(this.batch, lerp);
        this.batch.draw(this.silhouette, 0, 0, 800, 600);

        this.batch.setColor(this.overlay);
        this.batch.draw(this.blank, 0, 0, 800, 600);
        this.batch.setColor(ReadableColor.WHITE);

        this.batch.end();
    }

    public static float lerp(float prev, float now, double lerp) {
        return (float) (prev + (now - prev) * lerp);
    }

    public static void main(String[] args) {
        GameSettings settings = GameSettings.from("./assets/settings.cfg");
        new Thread(new Shroom(settings)).start();
    }

}
