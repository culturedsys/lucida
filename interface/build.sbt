name := "interface-component"

version := "0.1"

compile := {
  (Process("npm run build", baseDirectory.value) !)
  (compile in Compile).value
}

run := (Process("npm start", baseDirectory.value) !)