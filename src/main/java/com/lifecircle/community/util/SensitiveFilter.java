package com.lifecircle.community.util;

import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * 敏感词过滤
 * 1.定义前缀树
 * 2.根据敏感词，初始化前缀树
 * 3.编写过滤敏感词算法
 */
@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    // 替换符
    private static final String REPLACEMENT = "***";

    // 根节点
    private TrieNode rootNode = new TrieNode();

    // 1.定义前缀树
    private class TrieNode {

        // 关键词结束标识
        private boolean isKeywordEnd = false;

        // 子节点(key为下级字符，value为下级节点)
        private Map<Character, TrieNode> subNodes = new HashMap<>();

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeyWordEnd(boolean keyWordEnd) {
            isKeywordEnd = keyWordEnd;
        }

        public void addSubNode(Character c, TrieNode trieNode) {
            subNodes.put(c, trieNode);
        }

        public TrieNode getSubNode(Character c) {
            return subNodes.get(c);
        }
    }

    // 2.初始化前缀树
    // 注解的作用是：当容器实例化这个bean之后，这个方法自动调用
    @PostConstruct
    public void init() {
        try (
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                // 从上面这个字节流读取文件不太方便，将其转换成字符流，再把字符流转成缓冲流，这样读取效率更高
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        ) {
            String keyWord;
            // 一行一行读取，因为一个敏感词占一行
            while ((keyWord = reader.readLine()) != null) {
                // 将敏感词添加到前缀树
                this.addKeyword(keyWord);
            }
        } catch (Exception e) {
            logger.error("加载敏感词文件失败：" + e.getMessage());
        }
    }

    // 将一个敏感词添加到前缀树中
    private void addKeyword(String keyword) {
        TrieNode tempNode = rootNode;
        for (int i = 0; i < keyword.length(); i++) {
            char c = keyword.charAt(i);
            TrieNode subNode = tempNode.getSubNode(c);

            if (subNode == null) {
                subNode = new TrieNode();
                tempNode.addSubNode(c, subNode);
            }

            // 指向子节点，进入下一轮循环
            tempNode = subNode;

            if (i == keyword.length() - 1) {
                tempNode.setKeyWordEnd(true);
            }
        }
    }

    // 3.过滤敏感词算法
    public String filter(String text) {
        if (StringUtils.isBlank(text)) {
            return null;
        }

        // 指针1：遍历前缀数
        TrieNode tempNode = rootNode;
        // 指针2：遍历需要过滤的文本
        int begin = 0;
        // 指针3：遍历需要过滤的文本
        int position = 0;
        // 结果集
        StringBuilder sb = new StringBuilder();

        while (position < text.length()) {
            char c = text.charAt(position);

            // 跳过符号
            if (isSymbol(c)) {
                // 若指针1处于根节点，将此符号计入结果集，让指针2向前走一步
                if (tempNode == rootNode) {
                    sb.append(c);
                    begin++;
                }
                // 指针3放外面向前，因为指针1不处于根节点时，指针2不能走
                // 无论符号再开头或中间，指针3都向前走
                position++;
                continue;
            }

            // 检查下级节点
            tempNode = tempNode.getSubNode(c);
            if (tempNode == null) {
                // 以begin开头的字符串不是敏感词
                sb.append(text.charAt(begin));
                // 进入下一个位置
                position = ++begin;
                // 指针1重新指向根节点
                tempNode = rootNode;
            } else if (tempNode.isKeywordEnd()) {
                // 发现敏感词，将begin~position字符串替换掉
                sb.append(REPLACEMENT);
                // 进入下一个位置
                begin = ++position;
                // 指针1重新指向根节点
                tempNode = rootNode;
            } else {
                // 检查下一个字符
                position++;
            }
        }

        // 将最后一批字符计入结果
        sb.append(text.substring(begin));

        return sb.toString();
    }

    // 判断是否为符号
    private boolean isSymbol(Character c) {
        // 0x2E80~0x9FFF是东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }

}
