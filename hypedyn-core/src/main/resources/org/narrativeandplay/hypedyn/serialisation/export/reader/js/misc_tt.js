// MISC functions
		
// this object just represents a var
// only thing is we can set a callback to fire once this var is changed
function onready_obj() { // not used yet
	var obj = [];
	obj.set_value = function (new_val) {
		obj.value = new_val;
		onready(obj.value);
	}
	obj.onready = function (val) {}
}

function undefined_check( obj ) {
	return typeof( obj ) == 'undefined';
}

// remove self from its parent
function remove_self ( obj ) {
	if ( obj != null ) {
		if ( ! undefined_check ( obj.parentNode ) ) {
			obj.parentNode.removeChild(obj);
		} else {
			alert('remove self error on ' + obj );
		}
	}
}

  // count the number of entry via ( var in obj ) method
function obj_size( obj ) {
	var result = 0;
	for ( var i in obj ) {
		++result;
	}
	return result;
}

function clone_arr( arr ) { 
	return arr.slice(0);
}

// go through theme_data and get a list of the name
function theme_names() {
	var result = [];
	for ( var i in theme_data ) {
		var obj = [];
		if ( ! undefined_check ( theme_data[i].name ) ) {
			obj.name = theme_data[i].name
			result.push( obj );
		}
	}
	return result;
}

// a cross browser fast trimming ( remove spaces from front and back )
//http://stackoverflow.com/questions/3000649/trim-spaces-from-start-and-end-of-string
function trim (str) {
	str = str.replace(/^\s+/, '');
	for (var i = str.length - 1; i >= 0; i--) {
		if (/\S/.test(str.charAt(i))) {
			str = str.substring(0, i + 1);
			break;
		}
	}
	return str;
}

// this function deliberately ignores case - I think its not necessary for str as it was processed before, but leave for now - alex
// this function also ensures that there are no false positive (a word within another word's spelling) (eg hi in architecture)
// we make sure the site of the match on str starts and ends with a space.
function str_contains_phrase( str, substr ) {
	var match_index = str.toLowerCase().indexOf( substr.toLowerCase() );
	var match_found = ( match_index != -1 );

	// space delimiter check on match
	var front_check, back_check;
	var front_delimiter, back_delimiter;
	var valid_delimiter_arr = [ " ", ",", "." ];

	if ( match_found ) {
		// front check
		if ( match_index == 0 ) { // substr match at start of str
			front_check = true;
		} else {
			front_delimiter = str.substring(match_index - 1, match_index);
			front_check = valid_delimiter_check( front_delimiter, valid_delimiter_arr );
		}

		// back check
		if ( match_index + substr.length == str.length ) { // substr match ends at end of str
			back_check = true;
		} else {
			back_delimiter = str.substring( match_index + substr.length, match_index + substr.length + 1 );
			back_check = valid_delimiter_check( back_delimiter, valid_delimiter_arr );
		}
		return ( front_check && back_check );

	} else {
		return false;
	}

	//helper functions
	// str must match one of the valid delimiters
	function valid_delimiter_check( str, valid_delimiter_arr ) {
		var ret = false;
		for ( var i in valid_delimiter_arr ) {
			curr_delim = valid_delimiter_arr[i];
			ret = ret || ( str == curr_delim );
		}
		return ret;
	}
}

// unit test
function str_contains_phrase_test() {
	var test1 = str_contains_phrase( "architecture", "hi" ); 		var test1_ans = false;
	var test2 = str_contains_phrase( "arc hitecture", "hi" ); 		var test2_ans = false;
	var test3 = str_contains_phrase( "archi tecture", "hi" ); 		var test3_ans = false;
	var test4 = str_contains_phrase( "arc hi tecture", "hi" ); 		var test4_ans = true;
	var test5 = str_contains_phrase( "hi tecture", "hi" ); 			var test5_ans = true;
	var test6 = str_contains_phrase( "tecture hi", "hi" ); 			var test6_ans = true;
	var test7 = str_contains_phrase( "hitecture", "hi" ); 			var test7_ans = false;
	var test8 = str_contains_phrase( "tecturehi", "hi" ); 			var test8_ans = false;

	var result_arr = [ test1 == test1_ans, test2 == test2_ans, test3 == test3_ans, test4 == test4_ans,
		test5 == test5_ans, test6 == test6_ans, test7 == test7_ans, test8 == test8_ans ];

	for ( var i in result_arr ) {
		if ( ! result_arr[i] ) {
			//disp( 'test '+ i + ' failed ');
			return
		}
	}

	//disp( ' all test passed ');
}

// last_n starting from the last
// eg arr = [1,2,3,4,5]
// last_n ( arr, 3 ) --> [5,4,3]
function last_n ( arr, n ) {
	var ret = [];
	for (var i=0; i<n; i++) {
		var index = arr.length - i - 1;
		if(index>=0)
			ret.push( arr[ index ] );
	}
	return ret;
}

function last( arr ) {
	return arr[arr.length-1];
}

// given a set(arr) of set
// union all the sets into 1 set
function union( arr, dup ) {

	var ret = [];  // to return
	var tmp = []; // keep track of repeats

	for ( i in arr ) {
		var curr = arr[i];

		// for every array curr in arr,
		// go through every object in curr
		for ( j in curr ) {

			// if ret does not contain an object with a similar name
			// push it into ret
			if (  not_exist_dup( curr[j] ) ) {
				ret.push( curr[j] );
				tmp[curr.name] = 1;
			}
		}

		function not_exist_dup( curr ) {
			var ret = undefined_check( tmp[ curr.name ] ) ;
			//disp( 'not exist dup '+ret);
			return ret
		}
	}

	// we were given an array of many arrays
	// we return an array of one array (unioned)
	return [ ret ];
}

// returns an array without the entries at indices_arr
function remove_indices( arr, indices_arr ) {
	//disp('remove indices ');
	var ret = clone_arr(arr);

	// make sure it is in accending order (small to big)
	// clone first so we don't mess up the order of the original array! - alex
	var local_indices_arr = clone_arr( indices_arr );
	// sort numerically and ascending (ref http://www.javascriptkit.com/javatutors/arraysort.shtml) - alex
	local_indices_arr.sort(function(a,b){return a - b});

	// decrement all entry by 1 (assumes every entry is an integer)
	// used on local_indices_arr
	function decre_all( arr ) {
		for ( i in arr ) {
			arr[i] = arr[i] - 1 ;
		}
	}

	var curr_index = 0;

	for ( index_entry in local_indices_arr ) {
		//disp('index_entry ' + index_entry + '('+local_indices_arr+')');
		var curr_index = local_indices_arr[ index_entry ];
		// remove the entry at curr_index
		//disp('removing entry ' + curr_index);
		ret.splice( curr_index, 1 );

		// since one entry is removed, the index to trailing entries is reduced by 1
		decre_all( local_indices_arr );
	}

	return ret;
}

