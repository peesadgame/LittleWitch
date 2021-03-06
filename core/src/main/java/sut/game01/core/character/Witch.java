package sut.game01.core.character;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;
import playn.core.GroupLayer;
import playn.core.PlayN;
import playn.core.Sound;
import playn.core.util.Callback;
import sut.game01.core.Skill.Skill;
import sut.game01.core.all_etc.*;
import sut.game01.core.UI.FloatLabel;
import sut.game01.core.screen.Stage1;
import sut.game01.core.sprite.Sprite;
import sut.game01.core.sprite.SpriteLoader;

/**
 * Created by PSG on 3/13/14.
 */
public class Witch extends Character {

    public enum State {idleL,idleR,runL,runR,dead,atkR,atkL}

    private int level = 1;
    private int exp = 0;

    private boolean dead = false;
    private State state = State.idleR;

    public Witch(final GameEnvirontment gEnvir, final float x, final float y)
    {
        this.gEnvir = gEnvir;

        attack = 20 + VariableConstant.dmgLVL[gEnvir.gContent.getLevel()-1];
        maxHP = 150 + VariableConstant.hpLVL[gEnvir.gContent.getLevel()-1];
        hp = 150 + VariableConstant.hpLVL[gEnvir.gContent.getLevel()-1];
        defend = VariableConstant.defLVL[gEnvir.gContent.getLevel()-1];
        level = gEnvir.gContent.getLevel();
        exp = gEnvir.gContent.getExp();

        sprite = SpriteLoader.getSprite(VariableConstant.CharPeth[gEnvir.gContent.getCharID()]);
        sprite.addCallback(new Callback<Sprite>() {
            @Override
            public void onSuccess(Sprite result) {
                sprite.setSprite(spriteIndex);
                sprite.layer().setOrigin(75f / 2f, 84f / 2f);
                initPhysicsBody(gEnvir.world,x,y,75f - 40f,84f - 10f,false);
                body.setLinearDamping(1f);

                AllLayer.add(sprite.layer());
                createHPbar(sprite.layer().tx(),sprite.layer().ty() - 50f,70f);

                gEnvir.layer.add(AllLayer);

                ready = true;
            }

            @Override
            public void onFailure(Throwable cause) {

            }
        });
        owner = Owner.Ally;
    }

    @Override
    public void update(int delta) {
        super.update(delta);

        if (!ready || !alive) return;

        e+= delta;

        if (e > renderSpeed)
        {
            switch (state)
            {
                case idleR:
                    offset = 0;
                    sprite.layer().setOrigin(37.5f, 42f);
                    sprite.layer().setWidth(75);
                    break;
                case idleL:
                    offset = 4;
                    sprite.layer().setOrigin(37.5f, 42f);
                    sprite.layer().setWidth(75);
                    break;
                case runR:
                    offset = 8;
                    sprite.layer().setOrigin(37.5f, 42f);
                    sprite.layer().setWidth(75);
                    if(body.getLinearVelocity().x < 10)
                        body.applyForce(new Vec2(60f,0),body.getPosition());
                    break;
                case  runL:
                    offset = 8;
                    sprite.layer().setOrigin(-37.5f, 42f);
                    sprite.layer().setWidth(-75);
                    if(body.getLinearVelocity().x > -10) body.applyForce(new Vec2(-60f, 0), body.getPosition());
                    break;
                case dead:
                    offset = 12;
                    if(spriteIndex == 14){
                        alive = false;
                        dead = true;
                    }
                    break;
                case atkR:
                    offset = 16;
                    sprite.layer().setOrigin(37.5f, 42f);
                    sprite.layer().setWidth(75);
                    if(spriteIndex == 18) setState(State.idleR);
                    break;
                case atkL:
                    offset = 16;
                    sprite.layer().setOrigin(-37.5f, 42f);
                    sprite.layer().setWidth(-75);
                    if(spriteIndex == 18) setState(State.idleL);
                    break;
            }

            spriteIndex = offset + ((spriteIndex+1)%4);
            sprite.setSprite(spriteIndex);
            e = 0;
        }

        HPBar.setWidth(HPBarWidth * (hp/maxHP));
    }

    @Override
    public void paint() {
        super.paint();

        if (!ready) return;
            AllLayer.setTranslation(body.getPosition().x / VariableConstant.worldScale, body.getPosition().y / VariableConstant.worldScale);
    }

    @Override
    public void contact(DynamicObject A, DynamicObject B) {
        super.contact(A, B);


        if (!alive && state != State.dead) return;

        DynamicObject other;

        if(A.getClass() == this.getClass())
            other = B;
        else
            other = A;

        if(other.getBody().isBullet())
        {
            Skill skillObject = (Skill)other;
            if(skillObject.getOwner() != owner)
            {
                float dmg = skillObject.getDamage() - defend;
                if (dmg < 0) dmg = 1;

                hp = (hp - dmg) < 0 ? 0 : (hp - dmg);
                gEnvir.hpBarUI.needUpdate = true;

                HPBar.setWidth(HPBarWidth * (hp/maxHP));

                gEnvir.fLabel.CreateText((int) dmg, body.getPosition().x / VariableConstant.worldScale, (body.getPosition().y / VariableConstant.worldScale) - 15f);

                if (hp <= 0)
                {
                    renderSpeed = 150;
                    state = State.dead;
                }
                skillObject.destroy();
            }
        }
    }

    public void setState (State state)
    {
        if(state == State.dead) return;

        this.state = state;

        switch (state)
        {
            case idleL:
            case idleR:
            case dead:
                renderSpeed = 150;
                break;
            case runL:
            case runR:
                renderSpeed = 50;
                break;
            case atkR:
            case atkL:
                renderSpeed = 25;
                break;
        }

        spriteIndex = -1;
    }

    public State getState(){ return state;}

    public void jump()
    {
        if(body.getLinearVelocity().y == 0 && state != State.dead)
        {
            body.applyLinearImpulse(new Vec2(0f,-45f),body.getPosition());
            SoundStore.jump.play();
        }
    }

    public int getLevel()
    {
        return level;
    }

    public int getExp()
    {
        return exp;
    }

    public void gainEXP(int Exp)
    {
        exp += Exp;
        if(exp > VariableConstant.expRange[level-1])
        {
            if(level+1 <= 9)
            {
                exp = 0;
                level++;
                
                maxHP = 150 + VariableConstant.hpLVL[level-1];
                hp = 150 + VariableConstant.hpLVL[level-1];
                defend = VariableConstant.defLVL[level-1];

                SoundStore.levelup.play();
            }
            else
            {
                exp = VariableConstant.expRange[level-1];
            }
        }
        gEnvir.hpBarUI.needUpdate = true;
    }

    @Override
    public float getAttack() {
        return attack;
    }

    public boolean isDead()
    {
        return dead;
    }
}
