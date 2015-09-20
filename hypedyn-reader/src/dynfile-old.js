function loadStory() {
	setStartNode(1);
	createNode("start", "This is the story of a girl named Little Red Riding Hood, which she was called because of the red hood that she often wore.\n\nOne day she was walking through the forest.\n", false, 1);
	createLink(1, 94, 102, 20);
		createRule(20, "link", "if", "and", true, 21);
			createCondition(nodeVisited, [3], 21, false, 24);
			createAction("clickedLink", 21, gotoNode, [16], 25);
	createLink(1, 161, 167, 4);
		createRule(4, "link", "if", "and", true, 7);
			createAction("clickedLink", 7, gotoNode, [2], 17);

	createNode("forest", "In the forest, Red came across a young man with a nasty smile.\n\n\"Where are you going, little girl?\" he asked. \n\n\"I'm off to see my sick granny,\" she said.\n\nWell, you can probably guess what happened next.", false, 2);
	createLink(2, 199, 203, 10);
		createRule(10, "link", "if", "and", true, 11);
			createAction("clickedLink", 11, gotoNode, [3], 18);

	createNode("end", "*** The End ***\n\nback to start", false, 3);
	createLink(3, 17, 30, 13);
		createRule(13, "link", "if", "and", true, 14);
			createAction("clickedLink", 14, gotoNode, [1], 19);

	createNode("Hood details", "Her hood was a magic garment, given to her by her grandmother. It could kill anyone who tried to harm the wearer.\n\nBut not immediately. And in a most painful manner.\n\nMeanwhile, in the forest...\n", false, 16);
	createLink(16, 185, 191, 17);
		createRule(17, "link", "if", "and", true, 18);
			createAction("clickedLink", 18, gotoNode, [2], 20);


write_config_flag( 'back_button_flag', true );
write_config_flag( 'restart_button_flag', true );
write_config_flag( 'page_flipping_mode', true );
write_config_flag( 'window_resize_flag', true );
}