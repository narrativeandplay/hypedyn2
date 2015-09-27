function loadStory() {
	// try to avoid not-well-formed error (not working)
	jQuery.ajaxSetup({ scriptCharset: "utf-8" , contentType: "application/json; charset=utf-8"});

	// get the JSON file and parse it
	jQuery.getJSON( "LRRH-new.dyn", function( data ) {
		var story = data.story;
		var author = story.author; // unused
		var description = story.description; // unused
		var nodes = story.nodes;
		var facts = story.facts;

		// iterate through nodes and create
		for (var i=0; i < nodes.length; i++){
			var thisNode = nodes[i];
			if (thisNode!=null) {
				var id=thisNode.id; 			// id of the node
				var name=thisNode.name; 		// name of the node
				var content=thisNode.content; 	// node content
				var rules=thisNode.rules; 		// node rules - do this later

				// create the node
				createNode(name, content.text, false, id); // third param is isAnywhere??? check...

				// now create the links (if any)
				var links=content.rulesets;
				if(links!=null) {
					for(var j=0; j < links.length; j++) {
						var thisRuleset=links[j];				// this link ("ruleset")
						var start = thisRuleset.start;			// start offset of the link
						var end = thisRuleset.end;				// end offset of the link
						var rulesetID = thisRuleset.id;			// id of the link
						var rulesetName = thisRuleset.name;		// name of this link
						var rules = thisRuleset.rules;			// rules in this ruleset

						// create the link
						createLink(id, start, end, rulesetID);

						// now create rules (if any)
						if(rules!=null) {
							for(var k=0; k < rules.length; k++) {
								var thisRule=rules[k];					// this rule
								var ruleName=thisRule.name;				// name of this rule
								var stopIfTrue=thisRule.stopIfTrue;		// stop if true?
								var ruleID=thisRule.id;					// id of this rule
								var conditionsOp=thisRule.conditionsOp;	// operator (and/or)
								var conditions = thisRule.conditions;	// list of conditions
								var actions = thisRule.actions;			// list of actions

								// create the rule
								// createRule(parentID, parentType, if_not, and_or, fall_through, id)
								// do we have all this info?
								createRule(rulesetID, "link", "if", conditionsOp, stopIfTrue, ruleID);

								// now create the conditions and actions (if any)

								// create conditions
								if(conditions!=null) {
									for(var l=0; l < conditions.length; l++) {
										var thisCondition = conditions[l];					// this condition
										var conditionType = thisCondition.conditionType;	// type

										// createCondition(func, func_args_arr, ruleID, not, id)
										switch (conditionType) {
											case "NodeCondition":
												switch(thisCondition.params.status.value) {
													case "visited":
														createCondition(nodeVisited, [thisCondition.params.node.value], ruleID, false, l);
														break;
													case "not visited":
														createCondition(nodeVisited, [thisCondition.params.node.value], ruleID, true, l);
														break;
													case "is previous":
														createCondition(nodeIsPrevious, [thisCondition.params.node.value], ruleID, false, l);
														break;
													case "is not previous":
														createCondition(nodeIsPrevious, [thisCondition.params.node.value], ruleID, true, l);
														break;
													case "current":
														break;
												}
												break;
											case "LinkCondition":
												switch(thisCondition.params.status.value) {
													case "followed":
														createCondition(linkFollowed, [thisCondition.params.link.value], ruleID, false, l);
														break;
													case "not followed":
														createCondition(linkFollowed, [thisCondition.params.link.value], ruleID, true, l);
														break;
												}
												break;
											case "BooleanFactValue":
												switch(thisCondition.params.state) {
													case "true":
														createCondition(checkBoolFact, [thisCondition.params.fact], ruleID, false, l);
														break;
													case "false":
														createCondition(checkBoolFact, [thisCondition.params.fact], ruleID, true, l);
														break;
												}
												break;
											case "IntegerFactComparison":
												switch(thisCondition.params.comparisonValue) {
													case "input":
														// createCondition(compareNumFact, [19, '=', 'Input', 0], 27, false, 28);
														// value is missing here, bug #23
														createCondition(checkBoolFact, [thisCondition.params.fact,
															thisCondition.params.operator, 'Input', 0], ruleID, false, l);
														break;
													case "otherFact":
														//createCondition(compareNumFact, [19, '=', 'Fact', 19], 29, false, 30);
														createCondition(compareNumFact, [thisCondition.params.fact,
															thisCondition.params.operator, 'Fact', thisCondition.params.otherFact],
															ruleID, false, l);
														break;
												}
												break;
										}
									}
								}

								// create actions
								if(actions!=null) {
									for(var l=0; l < actions.length; l++) {
										var thisAction = actions[l];
										var actionType = thisAction.actionType;

										// createAction(eventType, parentRuleID, func, args, id)
										// eventType can be "enteredNode" "clickedLink" "anywhereCheck"
										switch (actionType) {
											case "LinkTo":
												createAction("clickedLink", ruleID, gotoNode, [thisAction.params.node.value], l);
												break;
											case "ShowPopupNode":
												break;
											case "UpdateText":
												break;
											case "UpdateBooleanFact":
												break;
											case "UpdateStringFact":
												break;
											case "EnableAnywhereLinkToHere":
												break;
											case "ShowDisabledAnywhereLink":
												break;
											case "UpdateIntegerFacts":
												break;
										}
									}
								}

							}
						}

					}
				}

				// now create the node rules (if any)

				// set start node if necessary
				if (thisNode.isStart) {
					setStartNode(id);
				}
			}
		}

		// add facts - skip for now

		// write config flags
		write_config_flag( 'back_button_flag', !story.metadata.backDisabled );
		write_config_flag( 'restart_button_flag', !story.metadata.restartDisabled );
		write_config_flag( 'page_flipping_mode', true );
		write_config_flag( 'window_resize_flag', true );

		// all of this was originally in window.onload
		read_config_flag();
	
		runhypedyn(); // entrance point of the story logic
		setTimeout('window.scrollTo(0, 0)', 1000); // for mobile to hide the url
	
		nonpageflip_init();
	});

	// set the start node
	/*
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
			*/
}