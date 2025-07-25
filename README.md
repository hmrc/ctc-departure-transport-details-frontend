# ctc-departure-transport-details-frontend

This service allows a user to complete the transport details section of a transit movement departure.

Service manager port: 10131

### Testing

Run unit tests:
<pre>sbt test</pre>
Run integration tests:
<pre>sbt it/test</pre>
Run accessibility linter tests:
<pre>sbt A11y/test</pre>

### Running manually or for journey tests
<pre>
sm2 --start CTC_TRADERS_P5_ACCEPTANCE
sm2 --stop CTC_DEPARTURE_TRANSPORT_DETAILS_FRONTEND
sbt run
</pre>

### Feature toggles

The following features can be toggled in [application.conf](conf/application.conf):

| Key                             | Argument type | sbt                                                           | Description                                                                                                                                                                                    |
|---------------------------------|---------------|---------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `feature-flags.phase-6-enabled` | `Boolean`     | `sbt -Dfeature-flags.phase-6-enabled=true run`                | If enabled, this will trigger customs-reference-data to retrieve reference data from crdl-cache.                                                                                               |
| `trader-test.enabled`           | `Boolean`     | `sbt -Dtrader-test.enabled=true run`                          | If enabled, this will override the behaviour of the "Is this page not working properly?" and "feedback" links. This is so we can receive feedback in the absence of Deskpro in `externaltest`. |
| `banners.showUserResearch`      | `Boolean`     | `sbt -Dbanners.showUserResearch=true run`                     | Controls whether or not we show the user research banner.                                                                                                                                      |
| `play.http.router`              | `String`      | `sbt -Dplay.http.router=testOnlyDoNotUseInAppConf.Routes run` | Controls which router is used for the application, either `prod.Routes` or `testOnlyDoNotUseInAppConf.Routes`                                                                                  |

### Updating external tasks

Due to the inherent link between transport equipment and items (a transport equipment goods reference corresponds to a consignment item declaration goods item number), we need logic to update the items task status for certain changes made to the transport equipment.
To handle this we assign a UUID to each transport equipment, with each page in the transport equipment journey (container identification number and seals pages) utilising the `appendTransportEquipmentUuidIfNotPresent` method to do this.
We also call the `updateItems` method as this will update the items task status to 'In progress' if the user needs to link an item to the newly added transport equipment as per rule C0670.
This redirects to the next page via the `/update-task` endpoint in `ctc-departure-items-frontend`.

### Scaffold

See [manage-transit-movements-departure-frontend](https://github.com/hmrc/manage-transit-movements-departure-frontend/blob/main/README.md#running-scaffold)

### User answers reader

See [manage-transit-movements-departure-frontend](https://github.com/hmrc/manage-transit-movements-departure-frontend/blob/main/README.md#user-answers-reader)

### Tampermonkey

See [manage-transit-movements-departure-frontend](https://github.com/hmrc/manage-transit-movements-departure-frontend/blob/main/README.md#tampermonkey-scripts)

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
