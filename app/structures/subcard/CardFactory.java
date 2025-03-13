package structures.subcard;

import structures.basic.Card;

/**
 * 卡牌工厂类，负责创建不同类型的卡牌实例
 * 这个类使用了工厂设计模式，将卡牌的创建与使用分离
 */
public class CardFactory {
    
    /**
     * 根据卡牌类型创建对应的卡牌实例
     * @param cardType 卡牌类型名称
     * @param id 卡牌ID
     * @return 创建的卡牌实例，如果类型不存在则返回null
     */
    public static Card createCard(String cardType, int id) {
        Card card = null;
        
        // 使用反射动态创建卡牌实例
        try {
            // 根据卡牌名称构造完整类名
            String className = "";
            
            // 玩家1卡牌
            if (isPlayer1Card(cardType)) {
                className = "structures.subcard.player1." + cardType;
            }
            // 玩家2卡牌
            else if (isPlayer2Card(cardType)) {
                className = "structures.subcard.player2." + cardType;
            }
            
            // 如果找到了类名，创建实例
            if (!className.isEmpty()) {
                Class<?> cardClass = Class.forName(className);
                card = (Card) cardClass.getDeclaredConstructor().newInstance();
                card.setId(id);
            }
        } catch (Exception e) {
            System.err.println("Error creating card of type " + cardType + ": " + e.getMessage());
            e.printStackTrace();
        }
        
        return card;
    }
    
    /**
     * 根据卡牌文件名创建卡牌
     * @param filename 卡牌配置文件名
     * @param id 卡牌ID
     * @return 创建的卡牌实例
     */
    public static Card createCardFromFilename(String filename, int id) {
        // 解析文件名获取卡牌类型
        // 例如将 "1_1_c_u_bad_omen.json" 转换为 "BadOmen"
        String[] parts = filename.split("_");
        StringBuilder cardTypeName = new StringBuilder();
        
        // 从第4个下划线后开始构建类名
        for (int i = 4; i < parts.length; i++) {
            String part = parts[i];
            // 处理最后一个部分（去掉.json后缀）
            if (i == parts.length - 1 && part.contains(".")) {
                part = part.substring(0, part.indexOf("."));
            }
            // 首字母大写
            cardTypeName.append(Character.toUpperCase(part.charAt(0)))
                         .append(part.substring(1));
        }
        
        return createCard(cardTypeName.toString(), id);
    }
    
    /**
     * 检查卡牌类型是否属于玩家1
     * @param cardType 卡牌类型名称
     * @return 如果是玩家1的卡牌则返回true
     */
    private static boolean isPlayer1Card(String cardType) {
        // 玩家1的卡牌类型列表
        String[] player1Cards = {
            "BadOmen", "GloomChaser", "RockPulveriser", "ShadowWatcher", 
            "NightsorrowAssassin", "BloodmoonPriestess", "Shadowdancer",
            "HornOfTheForsaken", "WraithlingSwarm", "DarkTerminus"
        };
        
        for (String card : player1Cards) {
            if (card.equals(cardType)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 检查卡牌类型是否属于玩家2
     * @param cardType 卡牌类型名称
     * @return 如果是玩家2的卡牌则返回true
     */
    private static boolean isPlayer2Card(String cardType) {
        // 玩家2的卡牌类型列表
        String[] player2Cards = {
            "SwampEntangler", "SilverguardSquire", "SkyrockGolem", 
            "SaberspineTiger", "SilverguardKnight", "YoungFlamewing",
            "IroncliffGuardian", "SundropElixir", "TrueStrike", "BeamShock"
        };
        
        for (String card : player2Cards) {
            if (card.equals(cardType)) {
                return true;
            }
        }
        return false;
    }
}