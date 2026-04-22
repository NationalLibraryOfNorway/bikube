{
  description = "Bikube development environment";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixpkgs-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs = { self, nixpkgs, flake-utils }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = import nixpkgs { inherit system; };
      in
      {
        devShells.default = pkgs.mkShell {
          buildInputs = with pkgs; [
            jdk21
            maven
            nodejs
            playwright-driver
            playwright-driver.browsers
          ];

          shellHook = ''
            export JAVA_HOME="${pkgs.jdk21}"
            export PLAYWRIGHT_BROWSERS_PATH="${pkgs.playwright-driver.browsers}"
            export PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1
            echo ""
            echo "🐝 Bikube dev shell ready!"
            echo ""
            echo "  mvn verify"
            echo "  mvn verify -Ddependency-check.skip=true"
            echo "  mvn test"
            echo ""
          '';
        };
      }
    );
}

