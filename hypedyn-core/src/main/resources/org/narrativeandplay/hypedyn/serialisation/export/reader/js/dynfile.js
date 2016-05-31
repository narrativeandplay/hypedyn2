
function createConditions(rulesetID, ruleID, conditions) {
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
				switch(thisCondition.params.state.value) {
					case "true":
						createCondition(checkBoolFact, [thisCondition.params.fact.value], ruleID, false, l);
						break;
					case "false":
						createCondition(checkBoolFact, [thisCondition.params.fact.value], ruleID, true, l);
						break;
				}
				break;
			case "IntegerFactComparison":
				var operator = thisCondition.params.operator.value;
				var not = false;

				// operators may not match, so adjust if necessary
				if (operator == "==") {
					operator = "=";
				}
				if (operator == "!=") {
					operator = "=";
					not = true;
				}

				switch(thisCondition.params.comparisonValue.value) {
					case "input":
						//createCondition(compareNumFact, [26, '=', 'Input', 1], 11, false, 51);
						createCondition(compareNumFact, [thisCondition.params.fact.value, operator,
							'Input', thisCondition.params.input.value], ruleID, not, l);
						break;
					case "otherFact":
						//createCondition(compareNumFact, [26, '=', 'Fact', 88], 11, false, 104);
						createCondition(compareNumFact, [thisCondition.params.fact.value, operator,
							'Fact', thisCondition.params.otherFact.value], ruleID, not, l);
						break;
				}
				break;
		}
	}
}

function createActions(rulesetID, ruleID, actions, isNodeRules) {
	for(var l=0; l < actions.length; l++) {
		var thisAction = actions[l];
		var actionType = thisAction.actionType;

		// createAction(eventType, parentRuleID, func, args, id)
		// eventType can be "enteredNode" "clickedLink" "anywhereCheck" "disabledAnywhereCheck"
		switch (actionType) {
			case "LinkTo":
				createAction("clickedLink", ruleID, gotoNode, [thisAction.params.node.value], l);
				break;
			case "ShowPopupNode":
				createAction("clickedLink", ruleID, popup, [thisAction.params.node.value], l);
				break;
			case "UpdateText":
				switch(thisAction.params.text.value) {
					case "textInput":
						//createAction("enteredNode", 63, replaceText, [62, "alternative text", "\"I'm not supposed to talk to strangers,\""], 98);
						createAction("enteredNode", ruleID, replaceText, [rulesetID, "alternative text", thisAction.params.textInput.value], l);
						break;
					case "stringFactValue":
						//createAction("enteredNode", 63, replaceText, [62, "text fact", 99], 101);
						createAction("enteredNode", ruleID, replaceText, [rulesetID, "text fact", thisAction.params.stringFactValue.value], l);
						break;
					case "NumberFactValue":
						//createAction("enteredNode", 4, replaceText, [3, "number fact", 2], 5);
						createAction("enteredNode", ruleID, replaceText, [rulesetID, "number fact", thisAction.params.NumberFactValue.value], l);
						break;
				}
				break;
			case "UpdateBooleanFact":
				//createAction("clickedLink", 4, setFact, [2, true], 5);
				createAction(isNodeRules ? "enteredNode" : "clickedLink", ruleID, setFact,
					[thisAction.params.fact.value, thisAction.params.value.value == "true"], l);
				break;
			case "UpdateStringFact":
				//createAction("clickedLink", 8, setFact, [6, "[the fact text...........]"], 9);
				createAction(isNodeRules ? "enteredNode" : "clickedLink", ruleID, setFact,
					[thisAction.params.fact.value, thisAction.params.value.value], l);
				break;
			case "UpdateIntegerFacts": // + - x / %
				switch (thisAction.params.updateValue.value) {
					case "inputValue":
						//createAction("clickedLink", 5, setNumberFact, [3, "Input", [10]], 6);
						createAction(isNodeRules ? "enteredNode" : "clickedLink", ruleID, setNumberFact,
							[thisAction.params.fact.value, "Input", [thisAction.params.inputValue.value]], l);
						break;
					case "integerFactValue":
						//createAction("clickedLink", 5, setNumberFact, [3, "Fact", [4]], 7);
						createAction(isNodeRules ? "enteredNode" : "clickedLink", ruleID, setNumberFact,
							[thisAction.params.fact.value, "Fact", [thisAction.params.integerFactValue.value]], l);
						break;
					case "computation":
						var operandtype1=thisAction.params.operand1.value;
						var operand1=thisAction.params[operandtype1].value;
						var operandtype2=thisAction.params.operand2.value;
						var operand2=thisAction.params[operandtype2].value;

						//createAction("clickedLink", 5, setNumberFact, [3, "Math", ["+", 3, "Fact", 4, "Fact"]], 10);
						createAction(isNodeRules ? "enteredNode" : "clickedLink", ruleID, setNumberFact,
							[thisAction.params.fact.value, "Math",
								[thisAction.params.operator.value,
									operand1, operandtype1 == "userOperand1" ? "Input" : "Fact",
									operand2, operandtype2 == "userOperand2" ? "Input" : "Fact"]],
							l);
						break;
					case "randomValue":
						var operandtype1=thisAction.params.start.value;
						var operand1=thisAction.params[operandtype1].value;
						var operandtype2=thisAction.params.end.value;
						var operand2=thisAction.params[operandtype2].value;

						//createAction("clickedLink", 5, setNumberFact, [3, "Random", [1, "Input", 4, "Fact"]], 11);
						createAction(isNodeRules ? "enteredNode" : "clickedLink", ruleID, setNumberFact,
							[thisAction.params.fact.value, "Random",
								[operand1, operandtype1 == "startInput" ? "Input" : "Fact",
									operand2, operandtype2 == "endInput" ? "Input" : "Fact"]],
							l);
						break;
				}
				break;
			case "EnableAnywhereLinkToHere":
				// hack to set anywhere flag in the node
				var node = nodelist[rulesetID];
				if(node!=null) {
					node.anywhere=true;
				}

				//createAction("anywhereCheck", 67, addAnywhereLink, [66], 68);
				createAction("anywhereCheck", ruleID, addAnywhereLink, [rulesetID], l);
				break;
			case "EnableThematicLinkToHere":
				// hack to set anywhere flag in the node
				var node = nodelist[rulesetID];
				if(node!=null) {
					node.anywhere=true;
				}

				//createAction("anywhereCheck", 67, addAnywhereLink, [66], 68);
				createAction("thematicAnywhereCheck", ruleID, addThematicAnywhereLink, [rulesetID], l);
				break;
			case "ShowDisabledAnywhereLink":
				//createAction("disabledAnywhereCheck", 2, addInactiveAnywhereLink, [1], 3);
				createAction("disabledAnywhereCheck", ruleID, addInactiveAnywhereLink, [rulesetID], l);
				break;
		}
	}
}

function createRules(rulesetID, rulesetType, rules, isNodeRules) {
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
		createRule(rulesetID, rulesetType, "if", conditionsOp, !stopIfTrue, ruleID);

		// now create the conditions and actions (if any)

		// create conditions
		if(conditions!=null) {
			createConditions(rulesetID, ruleID, conditions);
		}

		// create actions
		if(actions!=null) {
			createActions(rulesetID, ruleID, actions, isNodeRules);
		}
	}
}

function createFacts(facts) {
	for(var k=0; k < facts.length; k++) {
		var thisFact = facts[k];
		var factType = thisFact.type;
		var factID = thisFact.id;
		var factName = thisFact.name;
		var factValue = thisFact.initialValue;

		// create the fact
		switch(factType) {
			case "bool":
				createFact(factName, "boolean", factID);
				setFact(factID, factValue == "true");
				break;
			case "string":
				createFact(factName, "string", factID);
				setFact(factID, factValue);
				break;
			case "int":
				createFact(factName, "number", factID);
				setFact(factID, factValue);
				break;
		}
	}
}

function createThemes(themes) {
	for(var k=0; k < themes.length; k++) {
		var thisTheme = themes[k];
		var themeID = thisTheme.id;
		var themeName = thisTheme.name;
		var subthemes = thisTheme.subthemes;
		var submotifs = thisTheme.motifs;

		// create the theme
		disp("create theme: " + themeName)
		var corrected_theme_name = theme_correct_spelling( themeName.toLowerCase() ).toLowerCase();
		var obj = [];
		obj.name = corrected_theme_name;
		obj.id = themeID;
		obj.motifs = [];
		obj.subthemesByID = [];
		obj.subthemes = [];

		// iterate through motif_nodes and put into obj.motifs as strings
		for ( var i=0; i<submotifs.length; i++ ) {
			var motif_node = submotifs[i]; // this is an ID
			function findMotif(a) {
				return a.id == motif_node;
			}

			var the_motif = arr_find(motif_data, findMotif);
			if ( ! undefined_check( the_motif ) ) {
				var motif_str = the_motif.name;
				var corrected_motif = correct_spelling( motif_str.toLowerCase() ).toLowerCase();
				obj.motifs.push( corrected_motif );
			} else {
				//disp( motif_node );
				alert( 'undefined! ');
			}
		}

		// iterate through theme_nodes and put into obj.subthemes as ids
		// do we need to fix names with spaces here?
		for ( var j=0; j<subthemes.length; j++ ) {
			var theme_node = subthemes[j]; // this is an ID
			if ( ! undefined_check( theme_node ) ) {
				obj.subthemesByID.push( theme_node );
			}
		}

		theme_data[ corrected_theme_name ] = obj;
	}

	// now that all themes are loaded, attach the subthemes by name rather than by ID (to match the old algo)
	for ( var i in theme_data ) {
		var this_theme = theme_data[i];
		for (var j=0; j<this_theme.subthemesByID.length; j++) {
			var theme_node = this_theme.subthemesByID[j]; // this is an ID
			function findTheme(a) {
				thisID = a.id;

				return a.id == theme_node;
			}
			var actual_theme = arr_find(theme_data, findTheme);
			if ( ! undefined_check( actual_theme ) ) {
				this_theme.subthemes.push( actual_theme.name );
			} else {
				//disp( motif_node );
				alert( 'undefined! ');
			}
		}
	}
}

function createMotifs(motifs) {
	for(var k=0; k < motifs.length; k++) {
		var thisMotif = motifs[k];
		var motifID = thisMotif.id;
		var motifName = thisMotif.name;
		var features = thisMotif.features;

		// create the motif
		disp("create motif: " + motifName)
		var corrected_name = correct_spelling( motifName.toLowerCase() ).toLowerCase();
		var obj = [];
		obj.name = corrected_name;
		obj.id = motifID;
		obj.features = [];

		for (var i=0; i<features.length; i++ ) {
			var feature = features[i];
			if ( ! undefined_check( feature ) ) {
				obj.features.push( feature ); // assume only one component in a feature
			}
		}

		motif_data[corrected_name] = obj;
	}
}

function loadStory() {
	var data = getStoryData();
	var story = data.story;
	var author = story.author; // unused
	var description = story.description; // unused
	var nodes = story.nodes;
	var facts = story.facts;
	var themes = story.themes;
	var motifs = story.motifs;

	// iterate through nodes and create
	for (var i = 0; i < nodes.length; i++) {
		var thisNode = nodes[i];
		if (thisNode != null) {
			var id = thisNode.id; 			// id of the node
			var name = thisNode.name; 		// name of the node
			var content = thisNode.content; 	// node content
			var nodeRules = thisNode.rules; 		// node rules - do this later

			// create the node
			createNode(name, content.text, false, id); // third param is isAnywhere??? check...

			// now create the links (if any)
			var links = content.rulesets;
			if (links != null) {
				for (var j = 0; j < links.length; j++) {
					var thisRuleset = links[j];				// this link ("ruleset")
					var start = thisRuleset.start;			// start offset of the link
					var end = thisRuleset.end;				// end offset of the link
					var rulesetID = thisRuleset.id;			// id of the link
					var rulesetName = thisRuleset.name;		// name of this link
					var rules = thisRuleset.rules;			// rules in this ruleset

					// create the link
					createLink(id, start, end, rulesetID);

					// now create rules (if any)
					if (rules != null) {
						createRules(rulesetID, "link", rules, false);
					}
				}
			}

			// now create the node rules (if any)
			if (nodeRules != null) {
				createRules(id, "node", nodeRules, true);
			}

			// set start node if necessary
			if (thisNode.isStart) {
				setStartNode(id);
			}
		}
	}

	// add facts
	if (facts != null) {
		createFacts(facts);
	}

	// add motifs
	if (motifs != null) {
		createMotifs(motifs);
	}

	// add themes
	if (themes != null) {
		createThemes(themes);
	}

	// write config flags
	write_config_flag('back_button_flag', !story.metadata.backDisabled);
	write_config_flag('restart_button_flag', !story.metadata.restartDisabled);
	write_config_flag('page_flipping_mode', true);
	write_config_flag('window_resize_flag', true);
	write_config_flag('theme_threshold', story.metadata.themeThreshold);

	// all of this was originally in window.onload
	read_config_flag();

	// load thematic data
	//load_all_xml();

	runhypedyn(); // entrance point of the story logic
	setTimeout('window.scrollTo(0, 0)', 1000); // for mobile to hide the url

	nonpageflip_init();
}