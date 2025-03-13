package structures.subcard.player2;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.GameState;
import structures.basic.Tile;
import structures.basic.Unit;
import structures.subcard.CreatureCard;
import structures.subcard.ability.Flying;

import java.util.ArrayList;
import java.util.List;

/**
 * 幼年火翼龙 (Young Flamewing)
 * 费用：4
 * 攻击：5
 * 生命：4
 * 能力：飞行 - 可以移动到棋盘上的任何空格
 */
public class YoungFlamewing extends CreatureCard implements Flying {
    
    public YoungFlamewing() {
        setCardname("Young Flamewing");
        setManacost(4);
        setAttack(5);
        setHealth(4);
        setUnitConfig("conf/gameconfs/units/young_flamewing.json");
    }
    
    @Override
    public void onSummon(ActorRef out, GameState gameState, Unit unit, Tile tile) {
        // 飞行能力不需要在召唤时特殊处理
        // 它会影响单位的移动范围
    }
    
    @Override
    public List<Tile> getMovableTiles(Unit unit, GameState gameState) {
        List<Tile> movableTiles = new ArrayList<>();
        
        // 飞行单位可以移动到棋盘上的任何空格
        for (int x = 0; x < 9; x++) {
            for (int y = 0; y < 5; y++) {
                Tile tile = gameState.board[x][y];
                if (tile.getUnit() == null) {
                    movableTiles.add(tile);
                }
            }
        }
        
        return movableTiles;
    }
    
    @Override
    public void highlightMovableTiles(ActorRef out, Unit unit, GameState gameState) {
        // 高亮所有可移动的格子
        List<Tile> movableTiles = getMovableTiles(unit, gameState);
        
        for (Tile tile : movableTiles) {
            BasicCommands.drawTile(out, tile, 1); // 以高亮模式绘制格子
            gameState.movableTiles.add(tile);
        }
    }
}