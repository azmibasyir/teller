/*
 * Copyright 2017 The Mifos Initiative.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.mifos.teller.service.internal.service.helper;

import io.mifos.accounting.api.v1.client.AccountNotFoundException;
import io.mifos.accounting.api.v1.client.LedgerManager;
import io.mifos.accounting.api.v1.domain.AccountCommand;
import io.mifos.accounting.api.v1.domain.AccountEntryPage;
import io.mifos.accounting.api.v1.domain.JournalEntry;
import io.mifos.teller.ServiceConstants;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class AccountingService {

  private final Logger logger;
  private final LedgerManager ledgerManager;

  @Autowired
  public AccountingService(@Qualifier(ServiceConstants.LOGGER_NAME) final Logger logger,
                           final LedgerManager ledgerManager) {
    super();
    this.logger = logger;
    this.ledgerManager = ledgerManager;
  }

  public boolean accountExists(final String accountIdentifier) {
    try {
      this.ledgerManager.findAccount(accountIdentifier);
      return true;
    } catch (final AccountNotFoundException anfex) {
      this.logger.warn("Account {} not found.", accountIdentifier);
      return false;
    }
  }

  public AccountEntryPage fetchAccountEntries(final String accountIdentifier, final String dateRange, final Integer pageIndex,
                                              final Integer pageSize) {
    return this.ledgerManager.fetchAccountEntries(accountIdentifier, dateRange, pageIndex, pageSize, "identifier",
        Sort.Direction.DESC.name());
  }

  public void postJournalEntry(final JournalEntry journalEntry) {
    this.ledgerManager.createJournalEntry(journalEntry);
  }

  public void closeAccount(final String accountIdentifier) {
    final AccountCommand accountCommand = new AccountCommand();
    accountCommand.setAction(AccountCommand.Action.CLOSE.name());
    accountCommand.setComment(ServiceConstants.TX_CLOSE_ACCOUNT);
    this.ledgerManager.accountCommand(accountIdentifier, accountCommand);
  }

  public void openAccount(final String accountIdentifier) {
    final AccountCommand accountCommand = new AccountCommand();
    accountCommand.setAction(AccountCommand.Action.REOPEN.name());
    accountCommand.setComment(ServiceConstants.TX_OPEN_ACCOUNT);
    this.ledgerManager.accountCommand(accountIdentifier, accountCommand);
  }
}
