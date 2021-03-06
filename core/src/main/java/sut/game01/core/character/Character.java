package sut.game01.core.character;

import org.jbox2d.common.Vec2;
import playn.core.GroupLayer;
import playn.core.ImageLayer;
import static playn.core.PlayN.*;

import sut.game01.core.Rune.ItemRune;
import sut.game01.core.Skill.ItemCard;
import sut.game01.core.all_etc.DynamicObject;
import sut.game01.core.UI.FloatLabel;
import sut.game01.core.all_etc.GameEnvirontment;
import sut.game01.core.all_etc.ImageStore;
import sut.game01.core.all_etc.VariableConstant;
import sut.game01.core.screen.Stage1;
import sut.game01.core.sprite.Sprite;

import java.util.Random;

/**
 * Created by PSG on 3/13/14.
 */
public class Character extends DynamicObject {

    // Character Information
    protected float attack = 0;
    protected float defend = 0;
    protected float maxHP = 0;
    protected float hp = 0;
    protected Owner owner = null;

    // Character Model
    protected Sprite sprite = null;
    protected int spriteIndex = -1;
    protected int e = 0;
    protected int offset = 0;
    protected int renderSpeed = 150;
    protected GroupLayer AllLayer = graphics().createGroupLayer();

    // HPBar infomation
    protected boolean hasHPBar = false;
    protected ImageLayer HPBar;
    protected float HPBarWidth = 0;

    // Game Environtment
    protected GameEnvirontment gEnvir;

    public float getAttack() { return attack; }

    public float getDefend() { return defend;}

    public float getHp() { return hp;}

    public float getMaxHp() { return maxHP;}

    public Owner getOwner() {return owner;}

    public GroupLayer layer() {return AllLayer;}

    protected void AttackMain(Character focus, Vec2 distance){}

    public void createHPbar(float x, float y,float width)
    {
        ImageLayer HPBar = graphics().createImageLayer(ImageStore.HPBar);
        HPBar.setWidth(width);
        HPBar.setOrigin(HPBar.width() / 2f, HPBar.height() / 2f);
        HPBar.setTranslation(x,y);
        AllLayer.add(HPBar);

        hasHPBar = true;
        HPBarWidth = width;
        this.HPBar = HPBar;
    }

    protected Vec2 seekMain(Character focus)
    {
        if(focus.getBody() != null)
        {
            Vec2 distance = body.getLocalPoint(focus.getBody().getPosition());
            return distance;
        }
        else
        {
            return new Vec2(999f,0f);
        }
    }

    protected void  Move2Main(Character focus,Vec2 distance,float moveSpeed)
    {
        if(focus.getBody() == null) return;

        if(body.getLinearVelocity().x > -moveSpeed && body.getLinearVelocity().x < moveSpeed )

            if (distance.x > 0)
                body.applyForce(new Vec2(moveSpeed*3,0f),body.getPosition());
            else
                body.applyForce(new Vec2(-moveSpeed*3,0f),body.getPosition());
    }

    protected boolean inScreen(Character focus)
    {
        Vec2 tmp = seekMain(focus);
        if(tmp.x > 25 || tmp.x < -25 || focus.getBody() == null)
        {
            body.setActive(false);
            return false;
        }
        else
        {
            body.setActive(true);
            return true;
        }
    }

    public void setHp(float hp)
    {
       this.hp = hp;
       gEnvir.hpBarUI.needUpdate = true;
    }

    public void dropItem()
    {
        int chance = Math.abs((new Random()).nextInt()) % 100;
        if(chance < 10)
        {
            int RuneChance = Math.abs((new Random()).nextInt())  % 100;

            if(RuneChance < 10)
            {
                int runeID = Math.abs((new Random()).nextInt()) % VariableConstant.runeIDRange;
                gEnvir.tmpList.add(new ItemRune(gEnvir,new Vec2(body.getPosition().x, VariableConstant.worldGround - 2),runeID));
            }
            else
            {
                int itemID = Math.abs((new Random()).nextInt()) % VariableConstant.itemIDRange;
                gEnvir.tmpList.add(new ItemCard(gEnvir,new Vec2(body.getPosition().x, VariableConstant.worldGround - 2),itemID));
            }
        }
    }
}
