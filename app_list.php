<?php
	$con = mysqli_connect("localhost","crud","qwerty2020","notebamboo");
	$result = mysqli_query($con, "SELECT no, list, level, owner, AES_key, name, visible FROM list where user=".$_POST['user'].";");

	$response = array();
	while($row = mysqli_fetch_array($result)){
		array_push($response, $row);
	}

	echo json_encode(array("response" => $response));
	mysqli_close($con);
 ?>
