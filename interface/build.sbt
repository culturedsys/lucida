name := "interface-component"

compile := {
  (Process("npm run build", baseDirectory.value) !)
  (compile in Compile).value
}

run := (Process("npm start", baseDirectory.value) !)