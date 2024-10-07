package me.jack.LD56.entity;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJoint;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;
import com.badlogic.gdx.utils.Array;
import me.jack.LD56.Box2DHelper;
import me.jack.LD56.ColorUtils;
import me.jack.LD56.ai.AbstractAI;
import me.jack.LD56.ai.MoveToFoodAI;
import me.jack.LD56.ai.MoveToMateAI;
import me.jack.LD56.screen.GameScreen;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EntityBacteria extends Entity {


    @Override
    public int getEdgeThickness() {
        if(hasResistance){
            return super.getEdgeThickness()* 4;
        }
        return super.getEdgeThickness();
    }

    private Vector2 target;

    private Entity targetEntity;
    private float searchRadius = 100f;
    public final float targetProximityThreshold = 2f;

    private float waveTimer = 0;
    private final float waveFrequency = 2.0f;
    private final float waveAmplitude = 0.5f;
    private float forwardForceMagnitude =500.0f;

    public float hunger = 100f;
    public float maxhunger = 100f;
    private float scale;

    public int NUM_TAIL_SEGMENTS = 5;
    private float tailLength;
    private float tailWidth;

    private Body[] tailSegments;


    public AbstractAI currentBehaviour;

    public EntityBacteria( int x, int y, float scale,int tailSegments) {
        this( new Vector2(x, y), scale,tailSegments);
    }

    Vector2 startPos = null;

    public boolean onMateCooldown = true;
    public EntityBacteria(Vector2 position, float scale, int tailSegments){
        super(position.x, position.y);
        this.NUM_TAIL_SEGMENTS = tailSegments;
        if(scale >= 50)
            scale =50;
        startPos = position;
        forwardForceMagnitude = (float) (10 * Math.pow(scale,3));
        this.edgeColor = ColorUtils.getRandomColor();
        this.fillColor = ColorUtils.getRandomColor();
        this.NUM_TAIL_SEGMENTS = new Random().nextInt(4) + 2;
        this.target = null;
        this.scale = scale;
        this.tailLength = 2.0f * scale;
        this.tailWidth = 0.2f * scale;
        this.tailSegments = new Body[NUM_TAIL_SEGMENTS];
        maxhunger = (float) (10 * Math.pow(scale,4));
        hunger = maxhunger/2;

    }

    public boolean hasTail = true;

    public boolean hasEyes = false;
    public float eyeSize = 1f;
    public boolean createsPoison = false,hasResistance = false;
    public int poisonRate = 10;

    public float poisonLife = 5;
    public int poisonDamage = 40;
    private Body createBody(Vector2 position,World world) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(position);

        Body mainBody = world.createBody(bodyDef);

        PolygonShape mainBodyShape = new PolygonShape();
        mainBodyShape.setAsBox(1.0f * scale, 0.5f * scale);

        FixtureDef mainFixtureDef = new FixtureDef();
        mainFixtureDef.shape = mainBodyShape;
        float d = 50.0f *  GameScreen.ui.getCurrentValue("Creature Density") / 100.0f;
        System.out.println("New creature with density " + d);
        mainFixtureDef.density = 50.0f *  GameScreen.ui.getCurrentValue("Creature Density") / 100.0f;;
        mainFixtureDef.friction = 0.3f;
        mainFixtureDef.restitution = 0.5f;

        mainBody.createFixture(mainFixtureDef);
        mainBodyShape.dispose();

      //  if(new Random().nextInt(5) != 0) {
        if(hasTail)
            createTail(mainBody,world);
        if(hasEyes) {
            createEyes(mainBody, world);
        }
       // }else{
       //     hasTail = false;
       // }

        return mainBody;
    }

    private void createTail(Body mainBody,World world) {
        float segmentLength = tailLength / NUM_TAIL_SEGMENTS;

        for (int i = 0; i < NUM_TAIL_SEGMENTS; i++) {
            BodyDef segmentDef = new BodyDef();
            segmentDef.type = BodyDef.BodyType.DynamicBody;
            float xOffset = (1.0f * scale) + (i + 0.5f) * segmentLength;
            segmentDef.position.set(mainBody.getPosition().x + xOffset, mainBody.getPosition().y);

            Body segmentBody = world.createBody(segmentDef);
            segmentBody.setUserData(this);
            PolygonShape segmentShape = new PolygonShape();
            float segmentWidth = tailWidth * (1 - (float)i / NUM_TAIL_SEGMENTS);
            segmentShape.setAsBox(segmentLength / 2, segmentWidth / 2);

            FixtureDef segmentFixtureDef = new FixtureDef();
            segmentFixtureDef.shape = segmentShape;
            segmentFixtureDef.density = 0.5f;
            segmentFixtureDef.friction = 0.2f;

            segmentBody.createFixture(segmentFixtureDef);
            segmentShape.dispose();

            tailSegments[i] = segmentBody;

            // Create revolute joint
            RevoluteJointDef jointDef = new RevoluteJointDef();
            if (i == 0) {
                jointDef.bodyA = mainBody;
                jointDef.localAnchorA.set(1.0f * scale, 0);
            } else {
                jointDef.bodyA = tailSegments[i - 1];
                jointDef.localAnchorA.set(segmentLength / 2, 0);
            }
            jointDef.bodyB = segmentBody;
            jointDef.localAnchorB.set(-segmentLength / 2, 0);
            jointDef.enableLimit = true;
            jointDef.lowerAngle = (float) Math.toRadians(-15);
            jointDef.upperAngle = (float) Math.toRadians(15);

            world.createJoint(jointDef);
        }
    }

    private void calculateEntityArea() {
        float mainBodyArea = 2.0f * scale * 1.0f * scale;

        float tailArea = 0;
        for (int i = 0; i < NUM_TAIL_SEGMENTS; i++) {
            float segmentLength = tailLength / NUM_TAIL_SEGMENTS;
            float segmentWidth = tailWidth * (1 - (float)i / NUM_TAIL_SEGMENTS);
            float nextSegmentWidth = tailWidth * (1 - (float)(i+1) / NUM_TAIL_SEGMENTS);

            tailArea += (segmentWidth + nextSegmentWidth) * segmentLength / 2;
        }

        entityArea = mainBodyArea + tailArea;
    }
    public void waveTail(float deltaTime) {
        waveTimer += deltaTime;

        // Apply torque to simulate tail waving (this part stays the same)
        for (int i = 0; i < NUM_TAIL_SEGMENTS; i++) {
            float phaseOffset = (float) i / NUM_TAIL_SEGMENTS;
            float waveForce = MathUtils.sin(waveTimer * waveFrequency - phaseOffset * MathUtils.PI2) * waveAmplitude;
            tailSegments[i].applyTorque(waveForce, true);
        }

        // Calculate forward movement towards the target
        Vector2 bacteriaPosition = getBody().getPosition();

        // If no target, generate a random target
        if (target == null) {
            target = generateRandomTarget(bacteriaPosition, searchRadius);
        }

        // Calculate direction and distance to the target
        Vector2 directionToTarget = target.cpy().sub(bacteriaPosition);
        float distanceToTarget = directionToTarget.len();  // Get the distance to the target

        // Normalize the direction vector to get the direction
        directionToTarget.nor();

        // Adjust rotation: Rotate to face 180 degrees away from the target
        float desiredAngle = MathUtils.atan2(directionToTarget.y, directionToTarget.x);
        float currentAngle = getBody().getAngle();

        // Rotate 180 degrees
        float angleDifference = normalizeAngle(desiredAngle - currentAngle + MathUtils.PI);  // Adjust by π to rotate 180°

        // Apply torque to rotate toward the target smoothly
        float torque = angleDifference * 5.0f;  // Scaling factor for rotation smoothness
        getBody().applyTorque(torque, true);

        // Apply the force directly toward the target (preserving the original behavior)
        float maxForce = forwardForceMagnitude * scale * NUM_TAIL_SEGMENTS;
        Vector2 forwardForce = directionToTarget.scl(maxForce);
        getBody().applyForceToCenter(forwardForce, true);

        // Check if bacteria reached the target
        if (distanceToTarget < targetProximityThreshold || (targetEntity != null && targetEntity.dead)) {
            target = null;
            targetEntity = null;
        }
    }


    public void basicPush(){
        // Calculate forward movement towards the target
        Vector2 bacteriaPosition = getBody().getPosition();

        // If no target, generate a random target
        if (target == null) {
            target = generateRandomTarget(bacteriaPosition, searchRadius);
        }

        // Calculate direction and distance to the target
        Vector2 directionToTarget = target.cpy().sub(bacteriaPosition);
        float distanceToTarget = directionToTarget.len();  // Get the distance to the target

        // Normalize the direction vector to get the direction
        directionToTarget.nor();
        // Apply the force directly toward the target (preserving the original behavior)
        float maxForce = (forwardForceMagnitude/30) * scale;
        Vector2 forwardForce = directionToTarget.scl(maxForce);
        getBody().applyForceToCenter(forwardForce, true);

        // Check if bacteria reached the target
        if (distanceToTarget < targetProximityThreshold || (targetEntity != null && targetEntity.dead)) {
            target = null;
            targetEntity = null;
        }
    }
    private float normalizeAngle(float angle) {
        while (angle > MathUtils.PI) angle -= MathUtils.PI2;  // Normalize angle to be between -π and π
        while (angle < -MathUtils.PI) angle += MathUtils.PI2;
        return angle;
    }

        private Vector2 generateRandomTarget(Vector2 currentPosition, float radius) {
        float randomAngle = MathUtils.random(0, MathUtils.PI2);
        float randomDistance = MathUtils.random(0, radius);
        return new Vector2(
            currentPosition.x + MathUtils.cos(randomAngle) * randomDistance,
            currentPosition.y + MathUtils.sin(randomAngle) * randomDistance
        );
    }

    @Override
    public void createBody(World world) {
        Body body = createBody(startPos,world);
        setBody(body);

        calculateEntityArea();
        currentBehaviour = new MoveToFoodAI();
    }

    public float mateCooldown = 0;

    public float lastMate = 0;
    @Override
    public void update(float deltaTime,World world) {
        super.update(deltaTime,world);
        lastMate+= deltaTime;
        if(onMateCooldown){
            mateCooldown += deltaTime;
        }
        if(hasTail) {
            waveTail(deltaTime);
        }else{
            basicPush();
        }
        if(hasEyes) {
            moveEyes(deltaTime);
        }
        target = currentBehaviour.runAI(this,world);
        hunger -= (this.entityArea/50f) *  deltaTime * (20 * GameScreen.ui.getCurrentValue("Metabolism") /100.0f);
        if(hasTail){
            hunger -= ((this.entityArea/50f) *  deltaTime  * (30 * (NUM_TAIL_SEGMENTS+1) * GameScreen.ui.getCurrentValue("Metabolism") /100.0f))/2;
        }
        if(hasResistance){
            hunger -= ((this.entityArea/50f) *  deltaTime  * (30 * GameScreen.ui.getCurrentValue("Metabolism") /100.0f))/2;
        }
        if(createsPoison){
            hunger -= ((this.entityArea/50f) *  deltaTime  * (((poisonRate+poisonDamage)/100f) * GameScreen.ui.getCurrentValue("Metabolism") /100.0f))/2;
        }
        if(hasEyes){
            hunger -= ((this.entityArea/50f) *  deltaTime  * (eyeRadius/2 * GameScreen.ui.getCurrentValue("Metabolism") /100.0f))/2;

        }
        if(hunger <= 0) {
            this.dead = true;
        }
        if(hunger > (maxhunger/1.1) && !onMateCooldown) { //Only look for mate if not hungry
            MoveToMateAI ai = new MoveToMateAI();
            Vector2 matePos = ai.runAI(this, world);
            if (matePos != null) {
                currentBehaviour = ai;
                getBody().setLinearVelocity(0,0);
            }

            if (currentBehaviour instanceof MoveToMateAI && target == null) {
                //No mate in range, back to food
                currentBehaviour = new MoveToFoodAI();
            }
        }else{
            if(!(currentBehaviour instanceof  MoveToFoodAI)) {
                currentBehaviour = new MoveToFoodAI();
                getBody().setLinearVelocity(0, 0);
            }
        }
        if((lastMate > 10 || lifeTime > 90) && hunger >= (maxhunger/2)){
            if(new Random().nextInt(5) == 0) {
                System.out.println("Clone?");
                lastMate = 0;
                Vector2 pos = Box2DHelper.getRandomPointInCircle(getBody().getPosition().x,getBody().getPosition().y, 5f);
                EntityBacteria entity = new EntityBacteria((int) pos.x, (int) pos.y,scale, tailSegments.length);
                entity.edgeColor = this.edgeColor;
                entity.fillColor = this.fillColor;
                entity.hasTail = this.hasTail;
                GameScreen.spawn(entity);
            }
        }
        if(onMateCooldown){
          if(mateCooldown > 15){
              onMateCooldown = false;
              mateCooldown = 0;
          }
        }

        if(createsPoison && new Random().nextInt(poisonRate + 1) == 0) {
            Vector2 pos = Box2DHelper.getRandomPointInCircle(getBody().getPosition().x, getBody().getPosition().y, 5f);
            GameScreen.spawn(new EntityPoison(pos, 0.5f,poisonLife,poisonDamage,edgeColor,fillColor));
        }
        if(lifeTime > 100){
            if(new Random().nextInt(1000) == 0)
                dead = true;
        }
    }

    private Body[] eyeStalks = new Body[2];
    private float eyeLength;
    private float eyeRadius;
    private RevoluteJoint[] eyeJoints = new RevoluteJoint[2];

    private void createEyes(Body mainBody, World world) {
        if (eyeSize > 1.5 * scale) {
            eyeSize = (float) (1.5 * scale);
        }

        // Define lengths and size of eyes based on the scale
        eyeLength = 0.5f * scale * (eyeSize / 2);
        eyeRadius = 0.05f * scale * eyeSize;
        if(eyeRadius > 5){
            eyeRadius = 5;
            eyeLength = 10;
        }
        for (int i = 0; i < 2; i++) {
            BodyDef eyeStalkDef = new BodyDef();
            eyeStalkDef.type = BodyDef.BodyType.DynamicBody;

            // Position eyes at the front of the main body, opposite the tail
            float offsetX = -(1.0f * scale) - eyeLength / 2; // Negative X to place on the opposite side
            float offsetY = (i == 0 ? 0.3f * scale : -0.3f * scale); // Position the eyes slightly above and below the main body's centerline

            eyeStalkDef.position.set(mainBody.getPosition().x + offsetX, mainBody.getPosition().y + offsetY);

            // Create the eye stalk
            Body eyeStalkBody = world.createBody(eyeStalkDef);
            eyeStalkBody.setUserData(this);

            // Define the rod shape for the stalk
            PolygonShape eyeStalkShape = new PolygonShape();
            eyeStalkShape.setAsBox(eyeLength / 2, 0.05f * scale);

            FixtureDef eyeStalkFixtureDef = new FixtureDef();
            eyeStalkFixtureDef.shape = eyeStalkShape;
            eyeStalkFixtureDef.density = 1f;
            eyeStalkFixtureDef.friction = 0.1f;

            eyeStalkBody.createFixture(eyeStalkFixtureDef);
            eyeStalkShape.dispose();

            // Create the eyeball at the end of the stalk
            BodyDef eyeDef = new BodyDef();
            eyeDef.type = BodyDef.BodyType.DynamicBody;
            eyeDef.position.set(eyeStalkBody.getPosition().x - eyeLength / 2, eyeStalkBody.getPosition().y);

            Body eyeball = world.createBody(eyeDef);
            eyeball.setUserData(this);
            CircleShape eyeShape = new CircleShape();
            eyeShape.setRadius(eyeRadius);

            FixtureDef eyeFixtureDef = new FixtureDef();
            eyeFixtureDef.shape = eyeShape;
            eyeFixtureDef.density = 0.1f;
            eyeFixtureDef.friction = 0.1f;
            eyeball.createFixture(eyeFixtureDef);
            eyeShape.dispose();

            // Create a revolute joint between the eye stalk and the eyeball
            RevoluteJointDef eyeJointDef = new RevoluteJointDef();
            eyeJointDef.bodyA = eyeStalkBody;
            eyeJointDef.localAnchorA.set(-eyeLength / 2, 0);
            eyeJointDef.bodyB = eyeball;
            eyeJointDef.localAnchorB.set(0, 0);
            eyeJointDef.enableLimit = true;
            eyeJointDef.lowerAngle = (float) Math.toRadians(-15);
            eyeJointDef.upperAngle = (float) Math.toRadians(15);
            world.createJoint(eyeJointDef);

            // Store the stalk for future reference
            eyeStalks[i] = eyeStalkBody;

            // Create a revolute joint between the main body and the eye stalk with a motor
            RevoluteJointDef stalkJointDef = new RevoluteJointDef();
            stalkJointDef.bodyA = mainBody;
            stalkJointDef.localAnchorA.set(-1.0f * scale, offsetY); // Anchor on the negative side
            stalkJointDef.bodyB = eyeStalkBody;
            stalkJointDef.localAnchorB.set(eyeLength / 2, 0);
            stalkJointDef.enableLimit = true;
            stalkJointDef.lowerAngle = (float) Math.toRadians(-30);
            stalkJointDef.upperAngle = (float) Math.toRadians(30);
            stalkJointDef.enableMotor = true;
            stalkJointDef.motorSpeed = 0; // No continuous movement; just aims for a target angle.
            stalkJointDef.maxMotorTorque = 10.0f; // Adjust this value for the desired stiffness.

            // Create the joint and store it for adjusting the motor
            eyeJoints[i] = (RevoluteJoint) world.createJoint(stalkJointDef);
        }
    }

    private void moveEyes(float deltaTime) {
        // Aim to bring the eyes back to around a 45-degree angle.
        float targetAngle = MathUtils.PI / 4; // 45 degrees in radians

        for (int i = 0; i < eyeStalks.length; i++) {
            Body eyeStalk = eyeStalks[i];

            // Determine the current angle relative to the body's angle.
            float currentAngle = eyeStalk.getAngle() - getBody().getAngle();
            if (i == 1) {
                // For the second eye, aim for -45 degrees (mirrored angle).
                targetAngle = -MathUtils.PI / 4;
            }

            // Calculate the angle difference.
            float angleDifference = normalizeAngle(targetAngle - currentAngle);

            // Apply torque to gradually move the eye towards the target angle.
            float torque = angleDifference * 10.0f; // Adjust this value for smoother or faster movement.
            eyeStalk.applyTorque(torque, true);
        }
    }


    public void setTarget(Vector2 target) {
        this.target = target;
    }

    public Vector2 getTarget() {
        return target;
    }

    public void setScale(float newScale) {
        float oldScale = this.scale;
        this.scale = newScale;
        float scaleFactor = newScale / oldScale;

        Body mainBody = getBody();
        Vector2 bodyPosition = mainBody.getPosition();

        for (Body segment : tailSegments) {
            getBody().getWorld().destroyBody(segment);
        }

        while (mainBody.getJointList().size > 0) {
            getBody().getWorld().destroyJoint(mainBody.getJointList().get(0).joint);
        }

        while (mainBody.getFixtureList().size > 0) {
            mainBody.destroyFixture(mainBody.getFixtureList().first());
        }

        this.tailLength *= scaleFactor;
        this.tailWidth *= scaleFactor;
        createBody(bodyPosition,getBody().getWorld());

        searchRadius *= scaleFactor;
        forwardForceMagnitude *= scaleFactor;

        calculateEntityArea();
    }

    public float getScale() {
        return scale;
    }

    public float getEntityArea() {
        return entityArea;
    }

    public static boolean isWithin10Percent(float numA, float numB) {
        // Calculate 10% of numB
        float tolerance = 0.25f * numB;

        // Check if numA is within the range [numB - tolerance, numB + tolerance]
        return numA >= (numB - tolerance) && numA <= (numB + tolerance);
    }
    public void onCollision(Entity userDataB) {
        if (isWithin10Percent(this.entityArea, userDataB.entityArea) || userDataB.entityArea <= this.entityArea) {
            userDataB.dead = true;
            hunger += (50 * (GameScreen.ui.getCurrentValue("Food Nutrition") /100.0f)) * (userDataB.entityArea / 20);
            if(hunger > maxhunger)
                hunger = maxhunger;
         //   addExp(1.5f);
        }
    }
}
