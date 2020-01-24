#!/usr/local/bin/bash
set -eu
# Env variables
##LEDGERS_LINK="https://dev-psd2-ledgers.cloud.adorsys.de/actuator/info"
##CMS_LINK="https://dev-psd2-cms.cloud.adorsys.de/actuator/info"
##CONNECTOR_LINK="https://dev-psd2-xs2a.cloud.adorsys.de/actuator/info"
##ASPSP_PROFILE_LINK="https://dev-psd2-aspspprofile.cloud.adorsys.de/actuator/info"
##HELM_RELEASE="dev-psd2"
##ENV_JSON_FILE="psd2-env.json"
##PRINT VARS
#echo $LEDGERS_LINK\n$CMS_LINK\n$CONNECTOR_LINK\n$ASPSP_PROFILE_LINK\n$ENV_JSON_FILE

# Define array of arguments
myArray=( "$@" )
#define associative array to map args to repository
declare -A argsArray
argsArray[cms]="https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a"
argsArray[aspspprofile]="https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a"
argsArray[ledgers]="https://git.adorsys.de/adorsys/xs2a/ledgers"
argsArray[xs2a]="https://git.adorsys.de/adorsys/xs2a/xs2a-connector-examples"
BASE_JSON=$(jq . $ENV_JSON_FILE)
# loop for all element in arguments
for item_from_array in ${myArray[*]}
do
  # use tmp.json file to store temporary json result
  echo $BASE_JSON > tmp.json
  LINK="https://${HELM_RELEASE}-${item_from_array}.cloud.adorsys.de/actuator/info"
  LINK2="https://${HELM_RELEASE}-${item_from_array}.cloud.adorsys.de/swagger-ui.html"
  #get the version number
  JSON=$(curl -s $LINK | jq -r '.build')
  VERSION=$(echo $JSON | jq -r .version)
  #get the build number
  BUILD=$(echo $JSON | jq -r .build.number)
  #echo $BUILD
  # remove Build: from build.number
  BUILD=${BUILD//Build:/}
  #Echo $BUILD
  #create json element one per argument 
  for i in "${!argsArray[@]}"
  do
    if [[ $i == $item_from_array ]] && [ "$BUILD" != ""null ]; then 
      PIPELINE_LINK=${argsArray[$i]}
      #echo "$i and $item_from_array and $BUILD" 
      ONE_STRING_FIELD="{\"type\": \"mrkdwn\",\"text\": \"*$item_from_array:*\n Version: $VERSION \n build: $BUILD\n <$PIPELINE_LINK/pipelines/${BUILD}|Pipeline build link>\"}"
    fi
    if [[ $i == $item_from_array ]] && [[ "$BUILD" == "null" ]]; then
      #echo "ELSE $i and $item_from_array and $BUILD" 
      ONE_STRING_FIELD="{\"type\": \"mrkdwn\",\"text\": \"*$item_from_array:*\n Version: $VERSION \n build: $BUILD\n No Pipeline build link\"}"
    fi
  done
  #ONE_STRING_FIELD="{\"type\": \"mrkdwn\",\"text\": \"*$item_from_array:*\n Version: $VERSION \n build: $BUILD\n <$LINK2|Pipeline build link>\"}"
  #add to default json template into block.fields array one json element and use tmp file
  BASE_JSON=$(jq --argjson blockElement "$ONE_STRING_FIELD" '.blocks[2].fields += [$blockElement]' tmp.json)
done
#echo $BASE_JSON

#post to slack
STATUS_CODE=$(curl -sS -X POST -H 'Content-type: application/json' --data "$BASE_JSON" "$SLACK_CHANNEL")
echo $STATUS_CODE

exit 0