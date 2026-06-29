package com.ruthless.eventprocessor;

import com.ruthless.web.RuthlessClient;
import com.ruthless.web.request.RuthlessMemberLootItem;
import com.ruthless.web.request.RuthlessMemberLootRequest;
import lombok.Setter;
import lombok.Value;
import net.runelite.api.*;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.VarClientID;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.util.Text;
import net.runelite.http.api.loottracker.LootRecordType;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntToDoubleFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Map.entry;

/**
 * Most of the code from here is coming from Dink PetNotifier plugin. It does a great job of detecting when a pet
 * follows a player since we don't have a reliable way otherwise.
 */
public class PetEventProcessor {
    static final Pattern COLLECTION_LOG_REGEX = Pattern.compile("New item added to your collection log: (?<itemName>(.*))");
    public static final String UNTRADEABLE_WARNING = "Pet Notifier cannot reliably identify pet names unless you enable the game setting: Untradeable loot notifications";
    static final Pattern PET_REGEX = Pattern.compile("You (?:have a funny feeling like you|feel something weird sneaking).*");
    static final Pattern CLAN_REGEX = Pattern.compile("\\b(?<user>[\\w\\s]+) (?:has a funny feeling like .+ followed|feels something weird sneaking into .+ backpack|feels like .+ acquired something special): (?:(?<pet>.+) at (?<milestone>.+)|(?<pet2>.+))");
    private static final Pattern UNTRADEABLE_REGEX = Pattern.compile("Untradeable drop: (.+)");
    private static final Map<String, Integer> PET_NAMES_TO_WIKI_ITEM_ID;
    private static final String PRIMED_NAME = "";
    public static final int POPUP_PREFIX_LENGTH = "New item:".length();

    static final int MAX_TICKS_WAIT = 5;

    private @Inject Client client;

    private @Inject RuthlessClient ruthlessClient;

    private final AtomicInteger ticksWaited = new AtomicInteger();

    @Setter
    private volatile String petName = null;
    private volatile String milestone = null;
    private volatile boolean duplicate = false;
    private volatile boolean backpack = false;

    private volatile Boolean previouslyOwned = null;

    public void onChatMessage(String msg) {
        String chatMessage = sanitize(msg);
        if (petName == null) {
            if (PET_REGEX.matcher(chatMessage).matches()) {
                // Prime the notifier to trigger next tick
                this.petName = PRIMED_NAME;
                this.duplicate = chatMessage.contains("would have been");
                this.backpack = chatMessage.contains(" backpack");

                if (duplicate) {
                    this.previouslyOwned = true;
                }
            }
        } else {
            if (PRIMED_NAME.equals(petName) || previouslyOwned == null) {
                parseItemFromGameMessage(chatMessage)
                        .filter(item -> isPetItem(item.getItemName()))
                        .ifPresent(parseResult -> {
                            setPetName(parseResult.getItemName());
                            if (parseResult.isCollectionLog()) {
                                this.previouslyOwned = false;
                            }
                        });
            }
            if (previouslyOwned == null && chatMessage.contains("automatically insured")) {
                this.previouslyOwned = false;
            }
        }
    }

    public void onClanNotification(String message) {
        if (petName == null) {
            // We have not received the normal message about a pet drop, so this clan message cannot be relevant to us
            return;
        }

        Matcher matcher = CLAN_REGEX.matcher(message);
        if (matcher.find()) {
            String user = matcher.group("user").trim();
            if (user.equals(getPlayerName())) {
                var pet = matcher.group("pet");
                if (pet != null) {
                    this.petName = pet;
                    this.milestone = StringUtils.removeEnd(matcher.group("milestone"), ".");
                } else {
                    this.petName = matcher.group("pet2");
                }
            }
        }
    }

    public void onScript(int id) {
        if (id == ScriptID.NOTIFICATION_DELAY && PRIMED_NAME.equals(petName)) {
            var topText = client.getVarcStrValue(VarClientID.NOTIFICATION_TITLE);
            if ("Collection log".equalsIgnoreCase(topText)) {
                var bottomText = sanitize(client.getVarcStrValue(VarClientID.NOTIFICATION_MAIN));
                var itemName = bottomText.substring(POPUP_PREFIX_LENGTH).trim();
                if (isPetItem(itemName)) {
                    this.petName = itemName;
                    this.previouslyOwned = false;
                }
            }
        }
    }

    public void onTick() {
        if (petName == null)
            return;

        if (milestone != null || ticksWaited.incrementAndGet() > MAX_TICKS_WAIT) {

            this.handleNotify();
            this.reset();
        }
    }

    private void handleNotify() {
        Player local = client.getLocalPlayer();
        var request = RuthlessMemberLootRequest.builder()
                .sourceName("UNKNOWN")
                .world(client.getWorld())
                .groupSize(1)
                .addedBy(local.getName())
                .items(List.of(new RuthlessMemberLootItem(PET_NAMES_TO_WIKI_ITEM_ID.get(ucFirst(petName)),1,petName)))
                .build();
        ruthlessClient.submitLoot(request);
    }

    public void reset() {
        this.petName = null;
        this.milestone = null;
        this.duplicate = false;
        this.backpack = false;
        this.previouslyOwned = null;
        this.ticksWaited.set(0);
    }

    private boolean isPetItem(String itemName) {
        return itemName.startsWith("Pet ") || PET_NAMES_TO_WIKI_ITEM_ID.containsKey(ucFirst(itemName));
    }

    private static Optional<ParseResult> parseItemFromGameMessage(String message) {
        Matcher untradeableMatcher = UNTRADEABLE_REGEX.matcher(message);
        if (untradeableMatcher.find()) {
            return Optional.of(new ParseResult(untradeableMatcher.group(1), false));
        }

        Matcher collectionMatcher = COLLECTION_LOG_REGEX.matcher(message);
        if (collectionMatcher.find()) {
            return Optional.of(new ParseResult(collectionMatcher.group("itemName"), true));
        }

        return Optional.empty();
    }

    @Value
    private static class ParseResult {
        String itemName;
        boolean collectionLog;
    }

    static {
        PET_NAMES_TO_WIKI_ITEM_ID = Map.<String,Integer>ofEntries(
                entry("Abyssal orphan", ItemID.ABYSSALSIRE_PET),
                entry("Abyssal protector", ItemID.ABYSSALPET),
                entry("Baby chinchompa", ItemID.SKILLPETHUNTER_GREY),
                entry("Baby mole", ItemID.MOLEPET),
                entry("Baron", ItemID.DUKESUCELLUSPET),
                entry("Beef", ItemID.COWBOSSPET),
                entry("Bran", ItemID.RTBRANDAPET),
                entry("Butch", ItemID.VARDORVISPET),
                entry("Beaver", ItemID.SKILLPET_WC_OAK),
                entry("Bloodhound", ItemID.BLOODHOUND_PET),
                entry("Callisto cub", ItemID.CALLISTO_PET),
                entry("Chompy chick", ItemID.CHOMPYBIRD_PET),
                entry("Dom", ItemID.DOMPET),
                entry("Giant squirrel", ItemID.SKILLPETAGILITY),
                entry("Hellpuppy", ItemID.HELL_PET),
                entry("Herbi", ItemID.HERBIBOARPET),
                entry("Heron", ItemID.SKILLPETFISH),
                entry("Huberte", ItemID.HUEYPET),
                entry("Ikkle hydra", ItemID.HYDRAPET),
                entry("Jal-nib-rek", ItemID.INFERNOPET),
                entry("Kalphite princess", ItemID.KQPET_WALKING),
                entry("Lil' creator", ItemID.SOULWARSPET_RED),
                entry("Lil' zik", ItemID.VERZIKPET), // assume normal mode
                entry("Lil'viathan", ItemID.LEVIATHANPET),
                entry("Little nightmare", ItemID.NIGHTMAREPET), // assume team size 4
                entry("Moxi", ItemID.AMOXLIATLPET),
                entry("Muphin", ItemID.MUSPAHPET),
                entry("Nexling", ItemID.NEXPET),
                entry("Nid", ItemID.ARAXXORPET),
                entry("Noon", ItemID.DAWNPET),
                entry("Olmlet", ItemID.OLMPET),
                entry("Pet chaos elemental", ItemID.CHAOSELEPET),
                entry("Pet dagannoth prime", ItemID.PRIMEPET),
                entry("Pet dagannoth rex", ItemID.REXPET),
                entry("Pet dagannoth supreme", ItemID.SUPREMEPET),
                entry("Pet dark core", ItemID.CORPPET),
                entry("Pet general graardor", ItemID.BANDOSPET),
                entry("Pet k'ril tsutsaroth", ItemID.ZAMORAKPET),
                entry("Pet kraken", ItemID.KRAKENPET),
                entry("Pet penance queen", ItemID.PENANCEPET),
                entry("Pet smoke devil", ItemID.SMOKEPET),
                entry("Pet snakeling", ItemID.SNAKEPET),
                entry("Pet zilyana", ItemID.SARADOMINPET),
                entry("Phoenix", ItemID.PHOENIXPET),
                entry("Prince black dragon", ItemID.KBDPET),
                entry("Quetzin", ItemID.QUETZALPET),
                entry("Rift guardian", ItemID.SKILLPETRUNECRAFTING_AIR), // lava runes
                entry("Rock golem", ItemID.SKILLPETMINING), // gemstones
                entry("Rocky", ItemID.SKILLPETTHIEVING), // stalls
                entry("Scorpia's offspring", ItemID.SCORPIA_PET),
                entry("Scurry", ItemID.SCURRIUSPET),
                entry("Skotos", ItemID.SKOTIZOPET),
                entry("Smolcano", ItemID.ZALCANOPET),
                entry("Smol heredit", ItemID.SOLHEREDITPET),
                entry("Soup", ItemID.SKILLPETSAILING),
                entry("Sraracha", ItemID.SARACHNISPET),
                entry("Tangleroot", ItemID.SKILLPETFARMING), // mushrooms
                entry("Tiny tempor", ItemID.SKILLPETFISH_TEMPOROSS),
                entry("Tumeken's guardian", ItemID.WARDENPET_TUMEKEN),
                entry("Tzrek-jad", ItemID.JAD_PET),
                entry("Venenatis spiderling", ItemID.VENENATIS_PET),
                entry("Vet'ion jr.", ItemID.VETION_PET),
                entry("Vorki", ItemID.VORKATHPET),
                entry("Wisp", ItemID.WHISPERERPET),
                entry("Yami", ItemID.YAMAPET),
                entry("Youngllef", ItemID.GAUNTLETPET)
        );
    }

    /**
     * Converts text into "upper case first" form, as is used by OSRS for item names.
     *
     * @param text the string to be transformed
     * @return the text with only the first character capitalized
     */
    private String ucFirst(String text) {
        if (text.length() < 2) return text.toUpperCase();
        return Character.toUpperCase(text.charAt(0)) + text.substring(1).toLowerCase();
    }

    @Nullable
    private String getPlayerName() {
        Player player = client.getLocalPlayer();
        return player != null ? player.getName() : null;
    }

    private String sanitize(String str) {
        if (str == null || str.isEmpty()) return "";
        return Text.removeTags(str.replace("<br>", "\n")).replace('\u00A0', ' ').trim();
    }
}
