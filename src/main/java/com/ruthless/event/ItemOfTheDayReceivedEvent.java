package com.ruthless.event;

import com.ruthless.web.response.ItemOfTheDay;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ItemOfTheDayReceivedEvent {

    private ItemOfTheDay itemOfTheDay;
}
