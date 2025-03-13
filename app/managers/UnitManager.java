package managers;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.GameState;
import structures.basic.Card;
import structures.basic.EffectAnimation;
import structures.basic.Tile;
import structures.basic.Unit;
import structures.basic.UnitAnimationType;
import structures.subcard.CreatureCard;
import structures.subcard.ability.Flying;
import structures.subcard.player1.HornOfTheForsaken;
import utils.BasicObjectBuilders;
import utils.StaticConfFiles;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * UnitManager: 管理所有与单位相关的操作，包括召唤、移动、攻击和特殊能力。
 * 这个类是游戏逻辑的核心，处理所有单位的生命周期和交互。
 */
public class UnitManager {

    /**
     * 初始化并放置双方玩家的化身到起始位置
     * @param out WebSocket通信通道
     * @param gameState 当前游戏状态
     * @param p1Tile 玩家1的化身位置
     * @param p2Tile 玩家2的化身位置
     * @throws InterruptedException 确保UI更新平滑的延迟
     */
    public static void loadAndPlaceAvatars(ActorRef out, GameState gameState, Tile p1Tile, Tile p2Tile)
            throws InterruptedException {
        // 加载玩家1和玩家2的化身
        Unit p1Avatar = BasicObjectBuilders.loadUnit(StaticConfFiles.humanAvatar, 100, Unit.class);
        Unit p2Avatar = BasicObjectBuilders.loadUnit(StaticConfFiles.aiAvatar, 101, Unit.class);

        // 设置化身标记
        p1Avatar.setIsAvartar(1);
        p2Avatar.setIsAvartar(2);

        // 设置化身在棋盘上的位置
        p1Avatar.setPositionByTile(p1Tile);
        p2Avatar.setPositionByTile(p2Tile);

        // 将化身存储到游戏状态中
        gameState.setAvatars(p1Avatar, p2Avatar);

        p1Avatar.setOwner(1);
        p2Avatar.setOwner(2);

        // 设置攻击和生命值属性
        p1Avatar.setAttack(2);
        p1Avatar.setHealth(20);
        p2Avatar.setAttack(2);
        p2Avatar.setHealth(20);

        // 在棋盘上渲染化身
        BasicCommands.drawUnit(out, p1Avatar, p1Tile);
        Thread.sleep(50);
        BasicCommands.drawUnit(out, p2Avatar, p2Tile);
        Thread.sleep(50);

        // 更新UI以显示单位属性
        BasicCommands.setUnitAttack(out, p1Avatar, 2);
        BasicCommands.setUnitHealth(out, p1Avatar, 20);
        BasicCommands.setUnitAttack(out, p2Avatar, 2);
        BasicCommands.setUnitHealth(out, p2Avatar, 20);

        // 播放双方化身的闲置动画
        BasicCommands.playUnitAnimation(out, p1Avatar, UnitAnimationType.idle);
        BasicCommands.playUnitAnimation(out, p2Avatar, UnitAnimationType.idle);
    }

    /**
     * 在指定格子上召唤单位
     * @param out WebSocket通信通道
     * @param gameState 当前游戏状态
     * @param card 用于召唤单位的卡牌
     * @param clickedTile 目标格子
     * @return 创建的单位对象
     */
    public static Unit summonUnit(ActorRef out, GameState gameState, Card card, Tile clickedTile) {
        // 根据卡牌配置加载单位
        Unit newUnit = BasicObjectBuilders.loadUnit(card.getUnitConfig(), gameState.getCurrentUnitId(), Unit.class);
        newUnit.setOwner(gameState.currentPlayer);

        // 获取卡牌的攻击力和生命值
        int attackValue = card.getAttack();
        int healthValue = card.getHealth();

        // 确保单位至少有1点生命值（防止立即死亡）
        if (healthValue <= 0) {
            healthValue = 1;
        }

        // 设置单位的攻击力和生命值
        newUnit.setAttack(attackValue);
        newUnit.setHealth(healthValue);

        // 设置单位位置并标记格子为已占用
        newUnit.setTile(clickedTile);
        clickedTile.setUnit(newUnit);

        // 播放召唤特效
        try {
            BasicCommands.playEffectAnimation(out, BasicObjectBuilders.loadEffect(StaticConfFiles.f1_summon), clickedTile);
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 在UI上渲染单位
        BasicCommands.drawUnit(out, newUnit, clickedTile);
        
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 更新UI显示正确的攻击力和生命值
        BasicCommands.setUnitAttack(out, newUnit, attackValue);
        BasicCommands.setUnitHealth(out, newUnit, healthValue);

        // 播放单位闲置动画
        BasicCommands.playUnitAnimation(out, newUnit, UnitAnimationType.idle);

        // 将单位添加到单位列表
        gameState.playerUnits.add(newUnit);
        
        return newUnit;
    }

    /**
     * 高亮单位可以移动到的格子
     * @param out WebSocket通信通道
     * @param gameState 当前游戏状态
     * @param clickedTile 单位所在格子
     */
    public static void highlightMovableTile(ActorRef out, GameState gameState, Tile clickedTile) {
        // 清除之前的可移动格子
        gameState.movableTiles.clear();

        // 获取单位
        Unit unit = clickedTile.getUnit();
        if (unit == null || unit.getPosition() == null) {
            return;
        }
        
        // 如果单位被嘲讽，则不能移动
        if (gameState.isProvoked(unit)) {
            return;
        }
        
        // 检查单位是否有飞行能力
        if (unit instanceof Flying) {
            ((Flying) unit).highlightMovableTiles(out, unit, gameState);
            return;
        }
        
        // 普通单位移动范围（相邻、对角线和距离2的格子）
        int[][] directions = new int[][] {
                { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 }, // 相邻格子（右、左、下、上）
                { 2, 0 }, { -2, 0 }, { 0, 2 }, { 0, -2 }, // 距离2的格子
                { 1, 1 }, { 1, -1 }, { -1, 1 }, { -1, -1 } // 对角线格子
        };

        int cx = unit.getPosition().getTilex();
        int cy = unit.getPosition().getTiley();

        // 检查所有可能的移动目标
        for (int[] dir : directions) {
            int nx = cx + dir[0];
            int ny = cy + dir[1];

            if (nx >= 0 && nx < 9 && ny >= 0 && ny < 5) { // 确保在棋盘范围内
                Tile tile = gameState.board[nx][ny];
                if (tile.getUnit() == null) { // 只允许空格子
                    gameState.movableTiles.add(tile);
                    tile.setHighlightStatus(out, 1); // 在UI中高亮格子
                }
            }
        }
    }

    /**
     * 高亮单位可以攻击的格子
     * @param out WebSocket通信通道
     * @param gameState 当前游戏状态
     * @param clickedTile 单位所在格子
     */
    public static void highlightAttackableTile(ActorRef out, GameState gameState, Tile clickedTile) {
        // 清除之前的可攻击格子
        gameState.attackableTiles.clear();

        // 获取单位
        Unit unit = clickedTile.getUnit();
        if (unit == null || unit.getPosition() == null) {
            return;
        }
        
        // 检查周围是否有嘲讽单位，如果有，则只能攻击嘲讽单位
        List<Unit> provokeUnits = getAdjacentProvokeUnits(unit, gameState);
        if (!provokeUnits.isEmpty()) {
            for (Unit provokeUnit : provokeUnits) {
                Tile provokeUnitTile = provokeUnit.getTile();
                gameState.attackableTiles.add(provokeUnitTile);
                provokeUnitTile.setHighlightStatus(out, 2); // 在UI中高亮格子
            }
            return;
        }
        
        // 检查单位是否有远程攻击能力
        if (unit instanceof Ranged) {
            ((Ranged) unit).highlightAttackableTargets(out, unit, gameState);
            return;
        }
        
        // 普通单位攻击范围（相邻和对角线格子）
        int[][] directions = new int[][] {
                { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 }, // 相邻格子
                { 1, 1 }, { 1, -1 }, { -1, 1 }, { -1, -1 } // 对角线格子
        };

        int cx = unit.getPosition().getTilex();
        int cy = unit.getPosition().getTiley();

        // 检查所有可能的攻击目标
        for (int[] dir : directions) {
            int nx = cx + dir[0];
            int ny = cy + dir[1];

            if (nx >= 0 && nx < 9 && ny >= 0 && ny < 5) { // 确保在棋盘范围内
                Tile tile = gameState.board[nx][ny];

                // 如果格子是空的，跳过
                if (tile.getUnit() == null) {
                    continue;
                }

                // 只允许攻击敌方单位
                if (tile.getUnit().getOwner() != gameState.currentPlayer) {
                    gameState.attackableTiles.add(tile);
                    tile.setHighlightStatus(out, 2); // 在UI中高亮格子
                }
            }
        }
    }

    /**
     * 获取单位周围的嘲讽单位
     * @param unit 要检查的单位
     * @param gameState 当前游戏状态
     * @return 相邻的敌方嘲讽单位列表
     */
    private static List<Unit> getAdjacentProvokeUnits(Unit unit, GameState gameState) {
        List<Unit> result = new ArrayList<>();
        List<Unit> provokeUnits = gameState.getProvokeUnits();
        
        if (provokeUnits.isEmpty()) return result;
        
        int x = unit.getPosition().getTilex();
        int y = unit.getPosition().getTiley();
        
        for (Unit provokeUnit : provokeUnits) {
            if (provokeUnit.getOwner() == unit.getOwner()) continue; // 忽略同队的嘲讽单位
            
            int px = provokeUnit.getPosition().getTilex();
            int py = provokeUnit.getPosition().getTiley();
            
            // 如果嘲讽单位在相邻格子（包括对角线）
            if (Math.abs(x - px) <= 1 && Math.abs(y - py) <= 1) {
                result.add(provokeUnit);
            }
        }
        
        return result;
    }

    /**
     * 移动单位到另一个格子
     * @param out WebSocket通信通道
     * @param gameState 当前游戏状态
     * @param unit 要移动的单位
     * @param clickedTile 目标格子
     */
    public static void moveUnit(ActorRef out, GameState gameState, Unit unit, Tile clickedTile) {
        if (unit.getTile() == null) {
            return;
        }
        
        // 如果单位被嘲讽，不能移动
        if (gameState.isProvoked(unit)) {
            return;
        }
        
        System.out.println("manage: " + unit.getId());
        unit.getTile().setUnit(null);
        unit.setTile(clickedTile);
        clickedTile.setUnit(unit);
        BasicCommands.moveUnitToTile(out, unit, clickedTile);
        unit.addMoves();
        
        try {
            Thread.sleep(1000); // 等待移动动画完成
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 单位攻击目标
     * @param out WebSocket通信通道
     * @param gameState 当前游戏状态
     * @param attacker 攻击单位
     * @param target 目标单位
     */
    public static void attackUnit(ActorRef out, GameState gameState, Unit attacker, Unit target) {
        if (attacker == null || target == null) {
            return; // 确保攻击者和目标存在
        }

        // 如果有嘲讽单位但目标不是嘲讽单位，不能攻击
        List<Unit> provokeUnits = getAdjacentProvokeUnits(attacker, gameState);
        if (!provokeUnits.isEmpty() && !provokeUnits.contains(target)) {
            return;
        }

        // 计算伤害
        int attackDamage = attacker.getAttack();
        target.setHealth(target.getHealth() - attackDamage);

        // 确保生命值不小于0
        if (target.getHealth() <= 0) {
            target.setHealth(0);
        }

        // 如果目标是化身，更新玩家生命值
        if (target.getIsAvartar(1)) {
            gameState.player1.setHealth(target.getHealth());
            BasicCommands.setPlayer1Health(out, gameState.player1);
            
            // 触发头像受到伤害的事件
            triggerAvatarDamageEvents(out, gameState, 1);
        }
        if (target.getIsAvartar(2)) {
            gameState.player2.setHealth(target.getHealth());
            BasicCommands.setPlayer2Health(out, gameState.player2);
            
            // 触发头像受到伤害的事件
            triggerAvatarDamageEvents(out, gameState, 2);
        }

        // 播放攻击动画和更新UI
        BasicCommands.setUnitHealth(out, target, target.getHealth());
        BasicCommands.setUnitHealth(out, attacker, attacker.getHealth());
        playAnimation(out, attacker, UnitAnimationType.attack, 0);
        playAnimation(out, target, UnitAnimationType.hit, 1000);
        playAnimation(out, attacker, UnitAnimationType.idle, 0);
        playAnimation(out, target, UnitAnimationType.idle, 0);

        // 检查神器效果
        if (gameState.hasArtifact(target)) {
            int remainingDurability = gameState.decreaseArtifactDurability(target);
            if (remainingDurability <= 0) {
                // 神器已耗尽
                System.out.println("Artifact on unit " + target.getId() + " has been destroyed.");
            }
        }
        
        // 检查攻击者是否有神器
        if (gameState.hasArtifact(attacker)) {
            // 如果是玩家1的化身且拥有被遗忘者之角，触发特效
            if (attacker.getIsAvartar(1)) {
                HornOfTheForsaken.onAvatarDealDamage(out, gameState, attacker);
            }
        }

        // 如果目标死亡，移除单位
        if (target.getHealth() <= 0) {
            removeUnit(out, gameState, target);
        } else if (target.getAttack() > 0) {
            // 目标存活，进行反击（如果攻击者在目标的攻击范围内）
            boolean canCounter = isUnitInAttackRange(target, attacker);
            if (canCounter) {
                // 反击逻辑
                int counterDamage = target.getAttack();
                attacker.setHealth(attacker.getHealth() - counterDamage);
                
                // 确保生命值不小于0
                if (attacker.getHealth() <= 0) {
                    attacker.setHealth(0);
                }
                
                // 更新UI
                BasicCommands.setUnitHealth(out, attacker, attacker.getHealth());
                
                // 播放攻击动画
                playAnimation(out, target, UnitAnimationType.attack, 0);
                playAnimation(out, attacker, UnitAnimationType.hit, 1000);
                playAnimation(out, target, UnitAnimationType.idle, 0);
                playAnimation(out, attacker, UnitAnimationType.idle, 0);
                
                // 检查神器耐久度
                if (gameState.hasArtifact(attacker)) {
                    gameState.decreaseArtifactDurability(attacker);
                }
                
                // 如果攻击者死亡，移除单位
                if (attacker.getHealth() <= 0) {
                    removeUnit(out, gameState, attacker);
                }
            }
        }
        
        // 标记单位已攻击
        attacker.addAttacks();
    }

    /**
     * 检查目标单位是否在攻击者的攻击范围内（用于判断是否可以反击）
     * @param attacker 攻击者
     * @param target 目标
     * @return 如果目标在攻击范围内则返回true
     */
    private static boolean isUnitInAttackRange(Unit attacker, Unit target) {
        int ax = attacker.getPosition().getTilex();
        int ay = attacker.getPosition().getTiley();
        int tx = target.getPosition().getTilex();
        int ty = target.getPosition().getTiley();
        
        // 检查目标是否在相邻格子（包括对角线）
        return Math.abs(ax - tx) <= 1 && Math.abs(ay - ty) <= 1;
    }

    /**
     * 触发头像受到伤害时的相关事件（例如银卫骑士的热情效果）
     * @param out WebSocket通信通道
     * @param gameState 当前游戏状态
     * @param playerNum 受到伤害的玩家（1或2）
     */
    private static void triggerAvatarDamageEvents(ActorRef out, GameState gameState, int playerNum) {
        // 遍历所有单位，查找需要对头像伤害做出响应的单位
        for (Unit unit : gameState.playerUnits) {
            // 只处理属于被伤害的玩家方的单位
            if (unit.getOwner() == playerNum) {
                // 检查单位是否实现了Zeal接口
                if (unit instanceof Zeal) {
                    ((Zeal) unit).onAvatarDamaged(out, gameState, unit);
                }
                
                // 这里我们模拟一个简单的银卫骑士效果作为示例
                if (unit.getUnitConfig() != null && unit.getUnitConfig().contains("silverguard_knight")) {
                    int newAttack = unit.getAttack() + 2;
                    unit.setAttack(newAttack);
                    BasicCommands.setUnitAttack(out, unit, newAttack);
                    
                    // 播放增益特效
                    try {
                        BasicCommands.playEffectAnimation(out, BasicObjectBuilders.loadEffect(StaticConfFiles.f1_buff), unit.getTile());
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    
    /**
     * 远程攻击能力接口（用于支持有远程攻击能力的单位）
     */
    public interface Ranged {
        void highlightAttackableTargets(ActorRef out, Unit unit, GameState gameState);
    }
    
    /**
     * 热情能力接口（用于支持有热情能力的单位）
     */
    public interface Zeal {
        void onAvatarDamaged(ActorRef out, GameState gameState, Unit unit);
    }

    /**
     * 移除单位
     * @param out WebSocket通信通道
     * @param gameState 当前游戏状态
     * @param unit 要移除的单位
     */
    public static void removeUnit(ActorRef out, GameState gameState, Unit unit) {
        if (unit == null) return;
        
        // 移除单位之前存储它的引用，以免之后无法访问
        Tile unitTile = unit.getTile();
        
        // 标记格子为空
        if (unitTile != null) {
            unitTile.setUnit(null);
        }
        
        // 停止单位的死亡守望监听
        gameState.removeDeathwatchListeners(unit);
        
        // 如果是嘲讽单位，取消注册
        gameState.unregisterProvokeUnit(unit);
        
        // 播放死亡动画
        playAnimation(out, unit, UnitAnimationType.death, 2000);
        BasicCommands.deleteUnit(out, unit);
        
        // 从游戏状态中移除单位
        gameState.playerUnits.remove(unit);
        
        // 触发死亡守望效果
        gameState.triggerDeathwatch(unit);
    }

    /**
     * 播放单位动画
     * @param out WebSocket通信通道
     * @param unit 目标单位
     * @param type 动画类型
     * @param time 延迟时间
     */
    public static void playAnimation(ActorRef out, Unit unit, UnitAnimationType type, int time) {
        BasicCommands.playUnitAnimation(out, unit, type);
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            System.out.println("Thread interrupted.");
        }
    }
    
    /**
     * 创建并放置一个幽灵单位在指定格子
     * @param out WebSocket通信通道
     * @param gameState 当前游戏状态
     * @param tile 目标格子
     * @return 创建的幽灵单位
     */
    public static Unit summonWraithling(ActorRef out, GameState gameState, Tile tile) {
        // 检查格子是否为空
        if (tile == null || tile.getUnit() != null) {
            return null;
        }
        
        // 创建幽灵单位
        Unit wraithling = BasicObjectBuilders.loadUnit(StaticConfFiles.wraithling, gameState.getCurrentUnitId(), Unit.class);
        wraithling.setAttack(1);
        wraithling.setHealth(1);
        wraithling.setOwner(gameState.currentPlayer);
        
        // 设置单位位置
        wraithling.setTile(tile);
        tile.setUnit(wraithling);
        
        // 播放召唤特效
        try {
            BasicCommands.playEffectAnimation(out, BasicObjectBuilders.loadEffect(StaticConfFiles.f1_wraithsummon), tile);
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // 渲染单位
        BasicCommands.drawUnit(out, wraithling, tile);
        
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // 更新UI显示
        BasicCommands.setUnitAttack(out, wraithling, 1);
        BasicCommands.setUnitHealth(out, wraithling, 1);
        
        // 播放闲置动画
        BasicCommands.playUnitAnimation(out, wraithling, UnitAnimationType.idle);
        
        // 将单位添加到游戏状态
        gameState.playerUnits.add(wraithling);
        
        return wraithling;
    }
    
    /**
     * 查找单位周围的随机空格
     * @param gameState 当前游戏状态
     * @param unit 中心单位
     * @return 随机的相邻空格，如果没有空格则返回null
     */
    public static Tile getRandomAdjacentEmptyTile(GameState gameState, Unit unit) {
        if (unit == null || unit.getPosition() == null) {
            return null;
        }
        
        List<Tile> emptyTiles = new ArrayList<>();
        int x = unit.getPosition().getTilex();
        int y = unit.getPosition().getTiley();
        
        // 检查所有相邻和对角线格子
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) continue; // 跳过单位自身位置
                
                int nx = x + i;
                int ny = y + j;
                
                // 检查坐标是否合法
                if (nx >= 0 && nx < 9 && ny >= 0 && ny < 5) {
                    Tile tile = gameState.board[nx][ny];
                    // 如果格子为空，添加到候选列表
                    if (tile.getUnit() == null) {
                        emptyTiles.add(tile);
                    }
                }
            }
        }
        
        // 如果没有空格，返回null
        if (emptyTiles.isEmpty()) {
            return null;
        }
        
        // 返回随机的空格
        int randomIndex = (int)(Math.random() * emptyTiles.size());
        return emptyTiles.get(randomIndex);
    }
}